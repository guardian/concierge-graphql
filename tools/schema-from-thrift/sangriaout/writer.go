package sangriaout

import (
	"fmt"
	"io"
	"schema-from-thrift/thriftparser"
	"schema-from-thrift/utils"
)

func WriteField(w io.Writer, field thriftparser.ThriftElementFields, indentLevel int) error {
	indent := ""
	for i := 0; i < indentLevel; i++ {
		indent = indent + "  "
	}

	scalaField := field.DataTypeScala()
	var line string

	if scalaField == "" { //there is no translation available
		mappedType := "StringType"
		if field.IsOptional() {
			mappedType = "OptionalType(StringType)"
		}
		line = indent + fmt.Sprintf("Field(\"%s\",%s,None,resolve= ctx => JsonPath.root.%s.json.getOption(ctx.value).map(_.noSpaces)",
			field.FieldName(),
			mappedType,
			field.FieldName(),
		)
	} else {
		maybeGetter := ""
		if !field.IsOptional() {
			maybeGetter = ".get"
		}

		line = indent + fmt.Sprintf("Field(\"%s\",%s,None,resolve = ctx => JsonPath.root.%s.%s.getOption(ctx.value)%s),",
			field.FieldName(),
			field.DataTypeScala(),
			field.FieldName(),
			field.DataTypeJs(),
			maybeGetter,
		)
	}

	_, err := w.Write([]byte(line))
	return err
}

func writeLines(w io.Writer, linesPtr *[]string, indentLevel int) error {
	indent := ""
	for i := 0; i < indentLevel; i++ {
		indent = indent + "  "
	}

	for _, l := range *linesPtr {
		_, err := w.Write([]byte(indent + l + "\n"))
		if err != nil {
			return err
		}
	}
	return nil
}

func WriteEnumValue(w io.Writer, v thriftparser.EnumEntry, indentLevel int) error {
	lines := make([]string, 0)
	lines = append(lines, "EnumValue(")
	lines = append(lines, fmt.Sprintf(`  "%s",`, v.Name))
	lines = append(lines, `  Some("description needed"),`)
	lines = append(lines, fmt.Sprintf(`  "%s",`, v.Name))
	lines = append(lines, "),")
	return writeLines(w, &lines, indentLevel)
}

func WriteElement(w io.Writer, field thriftparser.ThriftElement, indentLevel int) error {
	indent := ""
	for i := 0; i < indentLevel; i++ {
		indent = indent + "  "
	}

	lines := make([]string, 10)
	varName := utils.Capitalise(field.Name())

	//If this is an enum, write out all values
	if enumValues := field.Values(); enumValues != nil {
		lines = append(lines, fmt.Sprintf("val %s = EnumType(", utils.Capitalise(varName)))
		lines = append(lines, fmt.Sprintf("  \"%s\",", varName))
		lines = append(lines, fmt.Sprintf("  Some(\"You need to put a comment in here\"),"))
		lines = append(lines, fmt.Sprintf("  List("))
		err := writeLines(w, &lines, indentLevel)
		if err != nil {
			return err
		}
		for _, v := range enumValues {
			err = WriteEnumValue(w, v, indentLevel+2)
			if err != nil {
				return err
			}
		}
		err = writeLines(w, &[]string{"  )", ")"}, indentLevel)
		if err != nil {
			return err
		}
	}
	//If this is a struct, write out all fields
	if subFields := field.Fields(); subFields != nil {
		lines = append(lines, fmt.Sprintf("val %s:ObjectType[Unit, Json] = ObjectType(", utils.Capitalise(varName)))
		lines = append(lines, fmt.Sprintf("  \"%s\",", varName))
		lines = append(lines, fmt.Sprintf("  \"You need to put a comment in here\","))
		lines = append(lines, "  ()=>{")
		lines = append(lines, "    fields[Unit, Json](")
	}
	return nil
}
