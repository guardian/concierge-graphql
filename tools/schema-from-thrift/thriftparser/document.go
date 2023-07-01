package thriftparser

import (
	"errors"
	"fmt"
	"os"
	"path"
)

type ThriftDocument interface {
	//Name gives the original filename of the document
	Name() string
	//Elements returns a list of all ThriftElements (structs, enums, etc.)
	Elements() []ThriftElement
	//ResolvedIncludes returns a list of all the Thrift documents that have been included. Note that they mut be resolved first
	ResolvedIncludes() []ThriftDocument
	//HasUnresolvedIncludes returns true if there are any included files that have not yet been resolved
	HasUnresolvedIncludes() bool
	//ResolveIncludes will load in the content for all included documents which have not yet been loaded. Call this or ResolvedIncludes() won't work.
	ResolveIncludes(basePath string) []error
}

type ThriftDocumentImpl struct {
	docName  string
	elements []ThriftElement
	includes map[string]ThriftDocument
}

func (d *ThriftDocumentImpl) Name() string {
	return d.docName
}

func (d *ThriftDocumentImpl) Elements() []ThriftElement {
	return d.elements
}

func (d *ThriftDocumentImpl) ResolvedIncludes() []ThriftDocument {
	result := make([]ThriftDocument, len(d.includes))

	i := 0
	for _, d := range d.includes {
		result[i] = d
		i++
	}
	return result
}

func (d *ThriftDocumentImpl) HasUnresolvedIncludes() bool {
	for _, i := range d.includes {
		if i == nil {
			return true
		}
	}
	return false
}

func (d *ThriftDocumentImpl) ResolveIncludes(basePath string) []error {
	problems := make([]error, 0)
	for fileName, content := range d.includes {
		if content != nil {
			continue
		}
		fullPath := path.Join(basePath, fileName)

		doc, err := func() (ThriftDocument, error) {
			fp, err := os.Open(fullPath)
			if err != nil {
				return nil, errors.New(fmt.Sprintf("could not open %s: %s", fullPath, err))
			}
			defer fp.Close()

			return Parse(fp, fileName)
		}()
		if err != nil {
			problems = append(problems, err)
		}
		d.includes[fileName] = doc
	}
	return problems
}
