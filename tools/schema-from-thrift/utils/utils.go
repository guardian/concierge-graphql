package utils

import "strings"

func Capitalise(str string) string {
	initial := string(str[0])
	return strings.ToTitle(initial) + str[1:]
}
