package thriftparser

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
	panic("dataTypeScala not implemented yet")
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
