package thriftparser

import "fmt"

type ThriftFieldPrimitive struct {
	dataType  string
	fieldName string
	index     int
	optional  bool
}

func (f *ThriftFieldPrimitive) IsPrimitiveType() bool {
	return true
}

func (f *ThriftFieldPrimitive) TypeDefinition() ThriftElement {
	return nil
}

func (f *ThriftFieldPrimitive) DataType() string {
	return f.dataType
}

func (f *ThriftFieldPrimitive) DataTypeScala() string {
	t := func() string {
		switch f.dataType {
		case "string":
			return "StringType"
		case "i8":
			return "IntType"
		case "i16":
			return "IntType"
		case "i32":
			return "IntType"
		case "i64":
			return "BigIntType"
		case "float":
			return "FloatType"
		case "double":
			return "DoubleType"
		default:
			panic(fmt.Sprintf("type %s is not implemented in primitives.go", f.dataType))
		}
	}()
	if f.optional {
		return fmt.Sprintf("OptionalType(%s)", t)
	} else {
		return t
	}
}

func (f *ThriftFieldPrimitive) DataTypeJs() string {
	switch f.dataType {
	case "string":
		return "string"
	case "i8":
		return "int"
	case "i16":
		return "int"
	case "i32":
		return "int"
	case "i64":
		return "int"
	case "float":
		return "double"
	case "double":
		return "double"
	default:
		panic(fmt.Sprintf("type %s is not implemented in primitives.go", f.dataType))
	}
}
func (f *ThriftFieldPrimitive) FieldName() string {
	return f.fieldName
}

func (f *ThriftFieldPrimitive) Index() int {
	return f.index
}

func (f *ThriftFieldPrimitive) IsOptional() bool {
	return f.optional
}

func (f *ThriftFieldPrimitive) ResolveCustomFields(against ThriftDocument) bool {
	return true //resolving is a no-op on a primitive field
}
