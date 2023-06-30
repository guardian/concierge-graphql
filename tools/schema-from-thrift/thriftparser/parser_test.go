package thriftparser

import "testing"

func TestParseInElementLineNoElement(t *testing.T) {
	state := ParserState{
		namespaces:     map[string]string{"default": "test"},
		currentElement: nil,
		isInComment:    false,
	}
	more, err := parseInElementLine(&state, "    1: required i32 width")
	if more {
		t.Error("parsing a line out of object should result in more object=false")
	}
	if err == nil {
		t.Error("parsing a line out of object should return an error")
	}
}

func TestParseInElementLineWithStruct(t *testing.T) {
	state := ParserState{
		namespaces: map[string]string{"default": "test"},
		currentElement: &ThriftStructImpl{
			name:   "somestruct",
			fields: make([]ThriftElementFields, 0),
			source: nil,
		},
		isInComment: false,
	}
	more, err := parseInElementLine(&state, "    1: required i32 width")
	if !more {
		t.Error("parsing a line within the object that is not a close should result in more=true")
	}
	if err != nil {
		t.Errorf("got unexpected error when parsing a line within a struct: %s", err)
	}
	fields := state.currentElement.Fields()
	if len(fields) != 1 {
		t.Errorf("should have had 1 field on the resulting struct, got %d", len(fields))
	} else {
		if fields[0].FieldName() != "width" {
			t.Errorf("expected name 'width' got '%s'", fields[0].FieldName())
		}
		if fields[0].DataType() != "i32" {
			t.Errorf("expected type 'i32' got '%s'", fields[0].DataType())
		}
		if fields[0].Index() != 1 {
			t.Errorf("expected index 1 got %d", fields[0].Index())
		}
		if fields[0].IsOptional() {
			t.Errorf("required field should not be marked optional")
		}
	}
	if state.currentElement.Values() != nil {
		t.Errorf("struct element should return nil for enum values")
	}
}

func TestParseInElementLineWithEnum(t *testing.T) {
	state := ParserState{
		namespaces: map[string]string{"default": "test"},
		currentElement: &ThriftEnumImpl{
			name:    "someenum",
			entries: make([]EnumEntry, 0),
			source:  nil,
		},
		isInComment: false,
	}
	more, err := parseInElementLine(&state, "    PRIZE = 4,")
	if !more {
		t.Error("parsing a line within the object that is not a close should result in more=true")
	}
	if err != nil {
		t.Errorf("got unexpected error when parsing a line within an enum: %s", err)
	}
	if state.currentElement.Fields() != nil {
		t.Errorf("enum object should return nil for field entries")
	}

	values := state.currentElement.Values()
	if len(values) != 1 {
		t.Errorf("expected 1 enum value, got %d", len(values))
	} else {
		if values[0].Name != "PRIZE" {
			t.Errorf("expected enum value name to be 'PRIZE', got '%s", values[0].Name)
		}
		if values[0].Value != 4 {
			t.Errorf("expected enum value to be 4, got %d", values[0].Value)
		}
	}
}

func TestParseClosingInElementLine(t *testing.T) {
	state := ParserState{
		namespaces: map[string]string{"default": "test"},
		currentElement: &ThriftEnumImpl{
			name:    "someenum",
			entries: make([]EnumEntry, 0),
			source:  nil,
		},
		isInComment: false,
	}
	more, err := parseInElementLine(&state, "  }")
	if err != nil {
		t.Errorf("got unexpected error when parsing a closing line: %s", err)
	}
	if more {
		t.Errorf("parsing a closing line should signal no more object")
	}
}
