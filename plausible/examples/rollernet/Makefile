DITL_LIB:=../../../ditl.jar
PLAUSIBLE_LIB:=../../../plausible.jar

ORIG_DATA:=imote-traces-RollerNet.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/upmc/rollernet/imote-traces-RollerNet.tar.gz
TARGET:=rollernet.jar

DITL:=java -jar $(DITL_LIB)
JFLAGS:=-classpath "$(DITL_LIB):$(PLAUSIBLE_LIB):."

SHELL:=/bin/bash

.DEFAULT_GOAL:=$(TARGET)
.PHONY: clean proper


$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from CRAWDAD "
	@echo ""
	@read -p "CRAWDAD login: " login && \
	read -p "CRAWDAD password: " -s pass && \
	wget --password $$pass --user $$login $(CRAWDAD_URL)


$(TARGET): $(ORIG_DATA) InferRollernet.java
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
	@echo "   - Snapshots every 15 minutes"
	@echo "   - Keep only iMote data (nodes 1-62)"
	@echo ""
	$(DITL) graphs import-links --links all_links \
		--orig-time-unit s \
		--snap-interval 900 --offset -1156083900 \
	$@ imote-traces-RollerNet/contacts.dat
	$(DITL) graphs filter $@ all_links links 1-62
	$(DITL) rm $@ all_links
	$(DITL) graphs links-to-presence $@
	$(DITL) graphs import-groups --labels $@ members:1-26 staff:27-51 friends:52-62
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	javac $(JFLAGS) InferRollernet.java
	java $(JFLAGS) InferRollernet $@


clean:
	rm -rf *.class *.jar.tmp imote-traces-RollerNet

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)