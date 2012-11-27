DITL_DIR:=../../../
DITL_VERSION:=$(shell sed -n -r 's/.*<version>([^<]+)<\/version>.*/\1/p' $(DITL_DIR)/pom.xml | head -1)
DITL_LIB:=$(HOME)/.m2/repository/ditl/ditl/$(DITL_VERSION)/ditl-$(DITL_VERSION).jar

$(DITL_LIB):
	cd $(DITL_DIR) && mvn install


ORIG_DATA:=imote-trace3.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/cambridge/haggle/imote-trace3.tar.gz
TARGET:=infocom05.jar

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


$(TARGET): $(ORIG_DATA) $(DITL_LIB)
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data from CRAWDAD "
	@echo ""
	tar xzvf $<
	@echo ""
	@echo "#################################################"
	@echo " Importing infocom'05 trace"
	@echo "   - Keep only iMote data (nodes 1-41)"
	@echo ""
	$(DITL) graphs import-edges \
		--orig-time-unit s \
		$@ Exp3/contacts.Exp3.dat
	$(DITL) graphs edges-to-links --links all_links $@
	$(DITL) rm $@ edges
	$(DITL) filter $@ all_links links 1:41
	$(DITL) rm $@ all_links
	$(DITL) graphs links-to-presence $@
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible links-to-windowed-links $@ 1000
	$(DITL) plausible infer --update-interval 5 $@ 500 500
	$(DITL) rm $@ windowed_links

	javac $(JFLAGS) InferInfocom05.java
	java $(JFLAGS) InferInfocom05 $@


clean:
	rm -rf Exp3
	rm -rf *.jar.tmp

proper: clean
	rm -f $(ORIG_DATA) $(TARGET)