package thriftparser

import (
	"errors"
	"fmt"
	"regexp"
	"strings"
)

type ThriftElementFields interface {
	// IsPrimitiveType gives true if this element is a primitive otherwise false
	IsPrimitiveType() bool
	// TypeDefinition gives the ThriftElement describing the type, or null if this is a primitive
	TypeDefinition() ThriftElement
	// DataType gives the thrift data type of the element
	DataType() string
	// DataTypeScala the scala data-type of this element
	DataTypeScala() string
	// FieldName gives the name of the field
	FieldName() string
	//Index gives the numeric index
	Index() int
	//IsOptional returns true if the field is not marked as mandatory
	IsOptional() bool
}

type ThriftElement interface {
	// Name gives the name of the struct, enum, etc.
	Name() string
	// Fields gives a list of the fields that belong to this entry, if it has subfields. Otherwise nil.
	Fields() []ThriftElementFields
	// Values gives the enum values that belong to this entry, if it has any. Otherwise nil.
	Values() []EnumEntry
	SourceDoc() ThriftDocument
	Namespaces() map[string]string
}

//NewThriftElement parses a raw text line into an element structure
func NewThriftElement(sourceLine string, source ThriftDocument, namespaces map[string]string) (ThriftElement, error) {
	whitespace := regexp.MustCompile("\\s+")
	words := whitespace.Split(sourceLine, -1)

	if len(words) < 2 || !strings.HasSuffix(sourceLine, "{") {
		return nil, errors.New("source line does not start a thrift element")
	}
	switch words[0] {
	case "struct":
		elem := &ThriftStructImpl{
			name:       words[1],
			fields:     make([]ThriftElementFields, 0),
			source:     source,
			namespaces: &namespaces,
		}
		return elem, nil
	case "enum":
		elem := &ThriftEnumImpl{
			name:       words[1],
			entries:    make([]EnumEntry, 0),
			source:     source,
			namespaces: &namespaces,
		}
		return elem, nil
	default:
		return nil, errors.New(fmt.Sprintf("unrecognised element type: %s", words[0]))
	}
}
