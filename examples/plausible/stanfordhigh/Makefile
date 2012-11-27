DITL_DIR:=../../../
DITL_VERSION:=$(shell sed -n -r 's/.*<version>([^<]+)<\/version>.*/\1/p' $(DITL_DIR)/pom.xml | head -1)
DITL_LIB:=$(HOME)/.m2/repository/ditl/ditl/$(DITL_VERSION)/ditl-$(DITL_VERSION).jar

$(DITL_LIB):
	cd $(DITL_DIR) && mvn install


ORIG_DATA:=moteFiles.zip roles.zip
MOTES_URL:=http://www.salathegroup.com/data/school_2010/moteFiles.zip
ROLES_URL:=http://www.salathegroup.com/data/school_2010/roles.zip
TARGET:=stanford.jar

SHELL:=/bin/bash

DITL:=java -jar $(DITL_LIB)
JFLAGS:=-classpath "$(DITL_LIB):."

WGET=wget -O $(2) $(1)
CURL=curl $(1) > $(2)
GET=$(WGET); if [ $$? -eq 127 ]; then $(CURL); fi

.DEFAULT_GOAL:=$(TARGET)
.PHONY: clean proper


$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from www.salathegroup.com "
	@echo ""
	$(call GET,$(MOTES_URL),moteFiles.zip)
	$(call GET,$(ROLES_URL),roles.zip)

groups: roles.zip
	rm -f $@
	echo $$(grep student roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep staff roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep teacher roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep other roles/roles.txt | cut -f1) | tr ' ' , >> $@

$(TARGET): $(ORIG_DATA) $(DITL_LIB) ImportStanford.java
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data"
	@echo ""
	unzip -u moteFiles.zip
	unzip -u roles.zip
	@echo ""
	@echo "#################################################"
	@echo " Importing Penn school trace"
	@echo "   - Keep the 9 hours between 7AM and 4PM"
	@echo ""
	javac $(JFLAGS) ImportStanford.java
	java $(JFLAGS) ImportStanford $@ moteFiles/* 2>/dev/null
	$(DITL) graphs beacons-to-edges --beacons rand_beacons $@
	$(DITL) rm $@ rand_beacons
	$(DITL) graphs edges-to-links $@
	$(DITL) rm $@ edges
	$(DITL) graphs beacons-to-edges --beacons beacons $@
	$(DITL) rm $@ beacons
	$(DITL) graphs edges-to-links --links tmp_links $@
	$(DITL) rm $@ edges
	$(DITL) graphs buffer-links --links tmp_links --buffered-links sync_links $@ 0 20
	$(DITL) rm $@ tmp_links 
	$(DITL) graphs links-to-presence $@
	make groups
	$(DITL) graphs import-groups --labels student,staff,teacher,other $@ $$(cat groups)
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible links-to-windowed-links $@ 1000
	$(DITL) plausible infer --n-steps 10 --tau 200 $@ 1000 1000
	$(DITL) rm $@ windowed_links


clean:
	rm -f *.class
	rm -rf moteFiles roles __MACOSX
	rm -rf *.jar.tmp
	rm -f groups

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)