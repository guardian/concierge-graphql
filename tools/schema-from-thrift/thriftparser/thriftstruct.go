package thriftparser

type ThriftStructImpl struct {
	name       string
	fields     []ThriftElementFields
	source     ThriftDocument
	namespaces *map[string]string
}

func (e *ThriftStructImpl) Name() string {
	return e.name
}

func (e *ThriftStructImpl) Fields() []ThriftElementFields {
	return e.fields
}

func (e *ThriftStructImpl) SourceDoc() ThriftDocument {
	return e.source
}

func (e *ThriftStructImpl) Values() []EnumEntry {
	return nil
}

func (e *ThriftStructImpl) Namespaces() map[string]string {
	return *e.namespaces
}
