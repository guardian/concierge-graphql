package thriftparser

type ThriftDocument interface {
	Elements() []ThriftElement
}

type ThriftDocumentImpl struct {
	elements []ThriftElement
}

func (d *ThriftDocumentImpl) Elements() []ThriftElement {
	return d.elements
}
