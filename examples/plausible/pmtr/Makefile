DITL_LIB:=../../../dist/ditl.jar

ORIG_DATA:=pmtr.txt
CRAWDAD_URL:=http://www.crawdad.org/download/unimi/pmtr/pmtr.txt.gz
TARGET:=pmtr.jar

SHELL:=/bin/bash

DITL:=java -jar $(DITL_LIB)

WGET=wget --password $(4) --user $(3) -O $(2) $(1)
CURL=curl -u $(3):$(4) $(1) > $(2)
GET=$(WGET); if [ $$? -eq 127 ]; then $(CURL); fi

.DEFAULT_GOAL:=$(TARGET)
.PHONY: clean proper


$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from CRAWDAD "
	@echo ""
	@read -p "CRAWDAD login: " login && \
	read -p "CRAWDAD password: " -s pass && \
	$(call GET,$(CRAWDAD_URL),$(ORIG_DATA),$$login,$$pass)
	gunzip pmtr.txt.gz


$(TARGET): $(ORIG_DATA)
	@echo ""
	@echo "#################################################"
	@echo " Importing PMTR trace"
	@echo ""
	$(DITL) graphs import-edges \
		--orig-time-unit s \
		$@ pmtr.txt
	$(DITL) graphs edges-to-presence $@
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $@ 1800
	$(DITL) plausible infer --update-interval 5 $@ 500 500
	$(DITL) rm $@ windowed_edges


clean:
	rm -rf *.jar.tmp

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)