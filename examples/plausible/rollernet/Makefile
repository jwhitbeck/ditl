DITL_DIR:=../../../
DITL_VERSION:=$(shell sed -n -r 's/.*<version>([^<]+)<\/version>.*/\1/p' $(DITL_DIR)/pom.xml | head -1)
DITL_LIB:=$(HOME)/.m2/repository/ditl/ditl/$(DITL_VERSION)/ditl-$(DITL_VERSION).jar

$(DITL_LIB):
	cd $(DITL_DIR) && mvn install


ORIG_DATA:=imote-traces-RollerNet.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/upmc/rollernet/imote-traces-RollerNet.tar.gz
TARGET:=rollernet.jar

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
	@echo " Importing rollernet trace"
	@echo "   - Imported times are relative to first unix "
	@echo "     timestamp in the trace (1156083900)"
	@echo "   - Keep only iMote data (nodes 1-62)"
	@echo ""
	$(DITL) graphs import-edges --edges all_edges \
		--orig-time-unit s \
		--offset -1156083900 \
	$@ imote-traces-RollerNet/contacts.dat
	$(DITL) filter $@ all_edges edges 1:62
	$(DITL) rm $@ all_edges
	$(DITL) graphs edges-to-presence $@
	$(DITL) graphs import-groups --labels members,staff,friends $@ 1:26 27:51 52:62
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $@ 500
	$(DITL) plausible infer --constraints "LeftOutlier,node@38:Horizontal,node@38,height@25:RightOutlier,node@27:Horizontal,node@27,height@25" $@ 700 70
	$(DITL) rm $@ windowed_edges

clean:
	rm -rf *.jar.tmp imote-traces-RollerNet

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)