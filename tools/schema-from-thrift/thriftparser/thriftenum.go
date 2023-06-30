package thriftparser

type EnumEntry struct {
	Name  string
	Value int
}

type ThriftEnumImpl struct {
	name       string
	entries    []EnumEntry
	source     ThriftDocument
	namespaces *map[string]string
}

func (e *ThriftEnumImpl) Name() string {
	return e.name
}

func (e *ThriftEnumImpl) Fields() []ThriftElementFields {
	return nil
}

func (e *ThriftEnumImpl) SourceDoc() ThriftDocument {
	return e.source
}

func (e *ThriftEnumImpl) Values() []EnumEntry {
	return e.entries
}

func (e *ThriftEnumImpl) Namespaces() map[string]string {
	return *e.namespaces
}
