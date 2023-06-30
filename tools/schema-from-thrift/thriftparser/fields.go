package thriftparser

import (
	"errors"
	"fmt"
	"regexp"
)

func isPrimitive(dataType string) bool {
	knownPrimitives := []string{
		"string",
		"bool",
		"i8",
		"i16",
		"i32",
	}

	for _, t := range knownPrimitives {
		if dataType == t {
			return true
		}
	}
	return false
}

//isContainer will return a number indicating the count of 'slots' to fill and a boolean indicating if this is a container type
func isContainer(dataType string) (int, bool) {
	knownContainers := map[string]int{
		"list": 1,
		"map":  2,
	}

	if c, have := knownContainers[dataType]; have {
		return c, have
	} else {
		return 0, false
	}
}

func NewThriftField(fieldName string, dataType string, isOptional bool, index int) (ThriftElementFields, error) {
	var err error
	subXtractor := regexp.MustCompile("^(\\w+)<([\\w\\s,<>]+)>$")
	parts := subXtractor.FindAllStringSubmatch(dataType, -1)

	if parts != nil {
		slotCount, isC := isContainer(parts[0][1])
		if !isC {
			return nil, errors.New(fmt.Sprintf("found subfields %s requested on non-container type %s", parts[0][2], parts[0][1]))
		}

		subtypeSplit := regexp.MustCompile("\\s*,\\s*")
		subtypes := subtypeSplit.Split(parts[0][2], -1)
		if slotCount != len(subtypes) {
			return nil, errors.New(fmt.Sprintf("expected %d data types for %s but got %d", slotCount, dataType, len(subtypes)))
		}

		slots := make(map[int]ThriftElementFields, len(subtypes))
		for i, fieldType := range subtypes {
			slots[i], err = NewThriftField(
				fmt.Sprintf("%s.%d", fieldName, i),
				fieldType,
				false,
				i+1)
			if err != nil {
				return nil, err
			}
		}

		return &ThriftFieldContainer{
			slotCount: slotCount,
			slotTypes: subtypes,
			slots:     slots,
			dataType:  parts[0][1],
			fieldName: fieldName,
			index:     index,
			optional:  isOptional,
		}, nil
	} else if isPrimitive(dataType) {
		return &ThriftFieldPrimitive{
			dataType,
			fieldName,
			index,
			isOptional,
		}, nil
	} else {
		return nil, errors.New("custom field types not implemented yet")
	}
}
