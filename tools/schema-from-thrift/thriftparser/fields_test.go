package thriftparser

import (
	"fmt"
	"testing"
)

func TestNewThriftFieldPrimitive(t *testing.T) {
	f, err := NewThriftField("name", "string", false, 4)
	if err != nil {
		t.Error("got an unexpected error creating primitive thrift field: ", err)
		t.FailNow()
	}
	if f.FieldName() != "name" {
		t.Error(fmt.Sprintf("got unexpected field name '%s'", f.FieldName()))
	}
	if f.Index() != 4 {
		t.Error(fmt.Sprintf("got unexpected index %d", f.Index()))
	}
	if f.IsOptional() {
		t.Error("expected isOptional to be false")
	}
	if !f.IsPrimitiveType() {
		t.Error("string should be a primitive type")
	}
	if f.DataType() != "string" {
		t.Error("got unexpected datatype ", f.DataType())
	}
	if f.TypeDefinition() != nil {
		t.Error("type definition for a primitive type should be nil")
	}
	if _, isPrimitive := f.(*ThriftFieldPrimitive); !isPrimitive {
		t.Error("NewThriftField should have returned a ThriftFieldPrimitive for 'string'")
	}
}

func TestNewThriftFieldList(t *testing.T) {
	f, err := NewThriftField("name", "list<i32>", false, 4)
	if err != nil {
		t.Error("got an unexpected error creating list thrift field: ", err)
		t.FailNow()
	}
	if f.FieldName() != "name" {
		t.Error(fmt.Sprintf("got unexpected field name '%s'", f.FieldName()))
	}
	if f.Index() != 4 {
		t.Error(fmt.Sprintf("got unexpected index %d", f.Index()))
	}
	if f.IsOptional() {
		t.Error("expected isOptional to be false")
	}
	if f.IsPrimitiveType() {
		t.Error("list<string> should not be a primitive type")
	}
	if f.DataType() != "list" {
		t.Error("got unexpected datatype ", f.DataType())
	}
	if f.TypeDefinition() != nil {
		t.Error("type definition for a primitive type should be nil")
	}
	list, isList := f.(*ThriftFieldContainer)
	if !isList {
		t.Error("NewThriftField should have returned ThriftFieldContainer for 'list'")
	} else {
		if list.slotCount != 1 {
			t.Error(fmt.Sprintf("expected slotCount to be 1, got %d", list.slotCount))
		}
		slot, haveSlot := list.slots[0]
		if !haveSlot {
			t.Error("expected a field in slot 1, did not get one")
		} else {
			if slot.FieldName() != "name.0" {
				t.Error("got unexpected subfield name ", slot.FieldName())
			}
			if !slot.IsPrimitiveType() {
				t.Error("i32 subfield should be a primitive")
			}
			if slot.DataType() != "i32" {
				t.Error("expected subfield type to be i32, got ", slot.DataType())
			}
			if slot.Index() != 1 {
				t.Error("expected subfield index to be 1, got ", slot.Index())
			}
		}
	}
}

func TestNewThriftFieldNestedMap(t *testing.T) {
	f, err := NewThriftField("name", "map<string, list<i32>>", false, 4)
	if err != nil {
		t.Error("got an unexpected error creating list thrift field: ", err)
		t.FailNow()
	}
	if f.FieldName() != "name" {
		t.Error(fmt.Sprintf("got unexpected field name '%s'", f.FieldName()))
	}
	if f.Index() != 4 {
		t.Error(fmt.Sprintf("got unexpected index %d", f.Index()))
	}
	if f.IsOptional() {
		t.Error("expected isOptional to be false")
	}
	if f.IsPrimitiveType() {
		t.Error("list<string> should not be a primitive type")
	}
	if f.DataType() != "map" {
		t.Error("got unexpected datatype ", f.DataType())
	}
	if f.TypeDefinition() != nil {
		t.Error("type definition for a primitive type should be nil")
	}
	list, isList := f.(*ThriftFieldContainer)
	if !isList {
		t.Error("NewThriftField should have returned ThriftFieldContainer for 'map'")
	} else {
		if list.slotCount != 2 {
			t.Error(fmt.Sprintf("expected slotCount to be 2, got %d", list.slotCount))
		}
		slot, haveSlot := list.slots[0]
		if !haveSlot {
			t.Error("expected a field in slot 1, did not get one")
		} else {
			if slot.FieldName() != "name.0" {
				t.Error("got unexpected subfield name ", slot.FieldName())
			}
			if !slot.IsPrimitiveType() {
				t.Error("string subfield should be a primitive")
			}
			if slot.DataType() != "string" {
				t.Error("expected subfield type to be string, got ", slot.DataType())
			}
			if slot.Index() != 1 {
				t.Error("expected subfield index to be 1, got ", slot.Index())
			}
		}

		nextSlot, haveNextSlot := list.slots[1]
		if !haveNextSlot {
			t.Error("expected a field in slot 2, did not get one")
		} else {
			if nextSlot.FieldName() != "name.1" {
				t.Error("got unexpected subfield name ", nextSlot.FieldName())
			}
			if nextSlot.IsPrimitiveType() {
				t.Error("list<i32> subfield should not be a primitive")
			}
			if nextSlot.DataType() != "list" {
				t.Error("expected subfield type to be list, got ", nextSlot.DataType())
			}
			if nextSlot.Index() != 2 {
				t.Error("expected subfield index to be 2, got ", nextSlot.Index())
			}
			subContainer, isSubContainer := nextSlot.(*ThriftFieldContainer)
			if !isSubContainer {
				t.Error("list<i32> subfield should also be a container")
			} else {
				if subContainer.slotCount != 1 {
					t.Error("list subfield slot count should be 1, got ", subContainer.slotCount)
				}
				if innerField, haveInner := subContainer.slots[0]; haveInner {
					if innerField.FieldName() != "name.1.0" {
						t.Error("unexpected name for sub-sub-field: ", innerField.FieldName())
					}
					if innerField.DataType() != "i32" {
						t.Error("unexpected datatype for sub-sub-field: ", innerField.DataType())
					}
				} else {
					t.Error("list subfield had no inner component")
				}
			}
		}
	}
}
