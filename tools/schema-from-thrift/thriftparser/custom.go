package thriftparser

type ThriftFieldCustom struct {
	fieldName    string
	dataTypeName string
	definition   ThriftElement
	index        int
	optional     bool
}

func (f *ThriftFieldCustom) IsPrimitiveType() bool {
	return false
}

func (f *ThriftFieldCustom) TypeDefinition() ThriftElement {
	return f.definition
}

func (f *ThriftFieldCustom) DataType() string {
	return f.dataTypeName
}

func (f *ThriftFieldCustom) DataTypeScala() string {
	panic("dataTypeScala not implemented yet")
}

func (f *ThriftFieldCustom) FieldName() string {
	return f.fieldName
}

func (f *ThriftFieldCustom) Index() int {
	return f.index
}

func (f *ThriftFieldCustom) IsOptional() bool {
	return f.optional
}

func (f *ThriftFieldCustom) ResolveCustomFields(against ThriftDocument) bool {
	if against == nil { //nothing to resolve against
		return false
	}
	if f.definition != nil { //we are already resolved
		return true
	}

	for _, elem := range against.Elements() {
		if elem.Name() == f.dataTypeName {
			f.definition = elem
			return true
		}
	}
	return false
}
