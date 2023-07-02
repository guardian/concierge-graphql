package thriftparser

import (
	"fmt"
	"log"
)

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
	switch f.dataType {
	case "list":
		innerType := f.slots[1].DataTypeScala()
		t := fmt.Sprintf("ListType(%s)", innerType)
		if f.IsOptional() {
			return fmt.Sprintf("OptionalType(%s)", t)
		} else {
			return t
		}
	case "map":
		log.Printf("WARN Field %s is a map type but map types are not properly supported in GraphQL", f.fieldName)
		return ""
	default:
		log.Printf("ERROR Field %s is of container type %s which is not implemented for Sangria yet", f.fieldName, f.dataType)
		return ""
	}
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

func (f *ThriftFieldContainer) DataTypeJs() string {
	switch f.dataType {
	case "list":
		return "arr"
	case "map":
		return ""
	default:
		log.Printf("ERROR Field %s is of container type %s which is not implemented for Sangria yet", f.fieldName, f.dataType)
		return ""
	}
}

func (f *ThriftFieldContainer) ResolveCustomFields(against ThriftDocument) bool {
	didResolve := true
	for _, slot := range f.slots {
		if !slot.IsPrimitiveType() {
			result := slot.ResolveCustomFields(against)
			if !result {
				didResolve = false
			}
		}
	}
	return didResolve
}
