NAME         = 'Dokumentacja'
PDF_FILENAME = $(NAME).pdf
TEX_FILENAME = $(NAME).tex
AUX_FILENAME = $(NAME).aux

.PHONY: $(PDF_FILENAME) all clean

all: $(PDF_FILENAME)

$(PDF_FILENAME):
	latexmk -pdf

clean:
	latexmk -C
