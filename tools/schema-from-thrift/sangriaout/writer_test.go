package sangriaout

import (
	"schema-from-thrift/thriftparser"
	"strings"
	"testing"
)

func TestWriteField(t *testing.T) {
	w := &strings.Builder{}
	testField, err := thriftparser.NewThriftField("myTestField", "string", false, 5)
	if err != nil {
		t.Error("unexpected error building test field: ", err)
		t.FailNow()
	}

	err = WriteField(w, testField, 0)
	written := w.String()

	expected := `Field("myTestField",StringType,None,resolve = ctx => JsonPath.root.myTestField.string.getOption(ctx.value).get),`
	if written != expected {
		t.Error("Field output did not render as expected.")
		t.Error("\tExpected: ", expected)
		t.Error("\tGot:      ", written)
	}
}

func TestWriteFieldOptional(t *testing.T) {
	w := &strings.Builder{}
	testField, err := thriftparser.NewThriftField("myTestField", "string", true, 5)
	if err != nil {
		t.Error("unexpected error building test field: ", err)
		t.FailNow()
	}

	err = WriteField(w, testField, 0)
	written := w.String()

	expected := `Field("myTestField",OptionalType(StringType),None,resolve = ctx => JsonPath.root.myTestField.string.getOption(ctx.value)),`
	if written != expected {
		t.Error("Field output did not render as expected.")
		t.Error("\tExpected: ", expected)
		t.Error("\tGot:      ", written)
	}
}

func TestWriteEnumValue(t *testing.T) {
	w := &strings.Builder{}

	testEnum := thriftparser.EnumEntry{
		Name:  "SOME_VALUE",
		Value: 153,
	}

	err := WriteEnumValue(w, testEnum, 0)
	if err != nil {
		t.Error("unexpected error writing enum value: ", err)
		t.FailNow()
	}

	expected := `EnumValue(
  "SOME_VALUE",
  Some("description needed"),
  "SOME_VALUE",
),
`
	written := w.String()
	if written != expected {
		t.Error("Enum output did not render as expected.")
		t.Error("\tExpected: ", expected)
		t.Error("\tGot:      ", written)
	}
}

func TestWriteElementEnum(t *testing.T) {
	w := &strings.Builder{}

	ns := map[string]string{}

	testData := thriftparser.NewThriftEnum(
		"someEnum",
		[]thriftparser.EnumEntry{
			{
				"FIRST_VALUE",
				1,
			},
			{
				"SECOND_VALUE",
				2,
			},
			{
				"THIRD_VALUE",
				3,
			},
		},
		nil,
		&ns,
	)

	err := WriteElement(w, testData, 0)
	if err != nil {
		t.Error("unexpected error writing out enum ", err)
		t.FailNow()
	}

	expected := `val SomeEnum = EnumType(
  "SomeEnum",
  Some("You need to put a comment in here"),
  List(
    EnumValue(
      "FIRST_VALUE",
      Some("description needed"),
      "FIRST_VALUE",
    ),
    EnumValue(
      "SECOND_VALUE",
      Some("description needed"),
      "SECOND_VALUE",
    ),
    EnumValue(
      "THIRD_VALUE",
      Some("description needed"),
      "THIRD_VALUE",
    ),
  )
)
`
	written := w.String()

	if written != expected {
		t.Error("Enum output did not render as expected.")
		t.Error("\tExpected: ", expected)
		t.Error("\tGot:      ", written)
	}
}
