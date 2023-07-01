package main

import (
	"flag"
	"log"
	"os"
	"path"
	"schema-from-thrift/thriftparser"
)

func main() {
	input := flag.String("in", "", "input thrift file to parse")
	flag.Parse()

	fp, err := os.Open(*input)
	if err != nil {
		log.Fatalf("ERROR Could not open %s: %s", *input, err)
	}
	defer fp.Close()

	dir, fileName := path.Split(*input)
	doc, err := thriftparser.Parse(fp, fileName)

	if err != nil {
		log.Fatalf("ERROR Could not parse %s: %s", *input, err)
	}

	if doc.HasUnresolvedIncludes() {
		log.Printf("INFO %s references some other documents", *input)
		var realDir string
		if dir == "" {
			realDir, _ = os.Getwd()
		} else {
			realDir = dir
		}
		loadErrs := doc.ResolveIncludes(realDir)
		if len(loadErrs) > 0 {
			for _, err := range loadErrs {
				log.Printf("WARNING Could not load included docs from %s: %s", realDir, err)
			}
		}
	}
	//spew.Dump(doc)
}
