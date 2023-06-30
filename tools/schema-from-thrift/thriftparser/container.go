package thriftparser

type ThriftFieldContainer struct {
	slotCount int
	slotTypes []string
	slots     map[int]ThriftElementFields
	dataType  string
	fieldName string
	index     int
	optional  bool
}

func (f *ThriftFieldContainer) IsPrimitiveType() bool {
	return false
}

func (f *ThriftFieldContainer) TypeDefinition() ThriftElement {
	return nil
}

func (f *ThriftFieldContainer) DataType() string {
	return f.dataType
}

func (f *ThriftFieldContainer) DataTypeScala() string {
	panic("dataTypeScala not implemented yet")
}

func (f *ThriftFieldContainer) FieldName() string {
	return f.fieldName
}

func (f *ThriftFieldContainer) Index() int {
	return f.index
}

func (f *ThriftFieldContainer) IsOptional() bool {
	return f.optional
}
