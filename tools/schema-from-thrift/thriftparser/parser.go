package thriftparser

import (
	"bufio"
	"errors"
	"fmt"
	"github.com/davecgh/go-spew/spew"
	"io"
	"regexp"
	"strconv"
)

type ParserState struct {
	namespaces     map[string]string
	currentElement ThriftElement
	isInComment    bool
}

//parseInElementLine reads a line when we know we are inside an element
//returns 'true' if the element continues or 'false' if it is ending
func parseInElementLine(state *ParserState, line string) (bool, error) {
	structEntry := regexp.MustCompile("^\\s*(\\d+):\\s*(required|optional)\\s*(\\w+)\\s*(\\w+)(\\s*=\\s*\\w+)*")
	enumEntry := regexp.MustCompile("^\\s*(\\w+)\\s*=\\s*(\\d+),*")
	blankLine := regexp.MustCompile("^\\s*$")
	elemEnd := regexp.MustCompile("^\\s*}")

	if blankLine.MatchString(line) {
		return true, nil
	} else if elemEnd.MatchString(line) {
		return false, nil
	} else if structEntry.MatchString(line) {
		if elem, isStruct := state.currentElement.(*ThriftStructImpl); isStruct {
			parts := structEntry.FindAllStringSubmatch(line, -1)
			indexNum, _ := strconv.ParseInt(parts[0][1], 10, 32)
			newField, err := NewThriftField(parts[0][4], parts[0][3], parts[0][2] != "required", int(indexNum))
			if err != nil {
				return false, err
			}
			elem.fields = append(elem.fields, newField)
			return true, nil
		} else {
			return false, errors.New("this line defines a struct field but not inside a struct")
		}
	} else if enumEntry.MatchString(line) {
		if elem, isEnum := state.currentElement.(*ThriftEnumImpl); isEnum {
			parts := enumEntry.FindAllStringSubmatch(line, -1)
			valueNum, _ := strconv.ParseInt(parts[0][2], 10, 32)
			newValue := EnumEntry{
				Name:  parts[0][1],
				Value: int(valueNum),
			}
			elem.entries = append(elem.entries, newValue)
			return true, nil
		} else {
			return false, errors.New("this line defines an enum field but not inside an enum")
		}
	} else {
		return false, errors.New("this line was not recognised")
	}
}

func parseOutOfElementLine(state *ParserState, doc *ThriftDocumentImpl, line string, whitespace *regexp.Regexp) error {
	var err error
	words := whitespace.Split(line, -1)
	quoteStripper := regexp.MustCompile(`^include\s*"(.*)"$`)
	spew.Dump(words)
	if len(words) < 2 {
		return nil
	}

	switch words[0] {
	case "namespace":
		if len(words) > 2 {
			state.namespaces[words[1]] = words[2]
		} else {
			state.namespaces["default"] = words[1]
		}
		break
	case "include":
		parts := quoteStripper.FindAllStringSubmatch(line, -1)
		if parts != nil {
			doc.includes[parts[0][1]] = nil
		} else {
			return errors.New("include directive found without file to include")
		}
	case "struct":
		if state.currentElement != nil {
			return errors.New("unexpected start of an element")
		}
		state.currentElement, err = NewThriftElement(line, doc, state.namespaces)
		doc.elements = append(doc.elements, state.currentElement)
		if err != nil {
			return err
		}
		break
	case "enum":
		if state.currentElement != nil {
			return errors.New("unexpected start of an element")
		}
		state.currentElement, err = NewThriftElement(line, doc, state.namespaces)
		doc.elements = append(doc.elements, state.currentElement)
		if err != nil {
			return err
		}
		break
	}
	return nil
}

func Parse(from io.Reader, docName string) (ThriftDocument, error) {
	isEmpty := regexp.MustCompile("^\\s*$")
	isComment := regexp.MustCompile("^\\s/+")
	commentEnd := regexp.MustCompile("\\*/")
	blockCommentStart := regexp.MustCompile("/\\*")
	whitespace := regexp.MustCompile("\\s+")
	state := ParserState{
		namespaces: make(map[string]string, 0),
	}

	doc := &ThriftDocumentImpl{
		docName:  docName,
		elements: make([]ThriftElement, 0),
		includes: make(map[string]ThriftDocument, 0),
	}

	s := bufio.NewScanner(from)
	lineCounter := 0
	var err error

	for s.Scan() {
		lineCounter++

		//if we are in an empty line or a line that only contains a comment then continue
		if isEmpty.MatchString(s.Text()) || isComment.MatchString(s.Text()) {
			continue
		} else if state.isInComment {
			if commentEnd.MatchString(s.Text()) {
				state.isInComment = false
				continue
			}
		}

		//if we are parsing in an element at the moment then look for in-element lines
		if state.currentElement != nil && !state.isInComment {
			shouldEndElem, err := parseInElementLine(&state, s.Text())
			if err != nil {
				return nil, errors.New(fmt.Sprintf("Parsing error at line %d: %s", lineCounter, err))
			}
			if shouldEndElem {
				state.currentElement = nil
			}
			continue
		}

		//otherwise look for root lines, by tokenising on spaces
		err = parseOutOfElementLine(&state, doc, s.Text(), whitespace)
		if err != nil {
			return nil, errors.New(fmt.Sprintf("Line %d: %s", lineCounter, err))
		}

		if blockCommentStart.MatchString(s.Text()) {
			state.isInComment = true
		}
	}
	return doc, nil
}
