DITL_LIB:=../../../ditl.jar
PLAUSIBLE_LIB:=../../../plausible.jar

ORIG_DATA:=imote-trace3.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/cambridge/haggle/imote-trace3.tar.gz
TARGET:=infocom05.jar

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


$(TARGET): $(ORIG_DATA) InferInfocom05.java
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data from CRAWDAD "
	@echo ""
	tar xzvf $<
	@echo ""
	@echo "#################################################"
	@echo " Importing infocom'05 trace"
	@echo "   - Snapshots every 30 minutes"
	@echo "   - Keep only iMote data (nodes 1-41)"
	@echo ""
	$(DITL) graphs import-edges \
		--orig-time-unit s \
		--snap-interval 1800 \
		$@ Exp3/contacts.Exp3.dat
	$(DITL) graphs edges-to-links --links all_links $@
	$(DITL) rm $@ edges
	$(DITL) graphs filter $@ all_links links 1-41
	$(DITL) rm $@ all_links
	$(DITL) graphs links-to-presence $@
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	javac $(JFLAGS) InferInfocom05.java
	java $(JFLAGS) InferInfocom05 $@


clean:
	rm -rf *.class
	rm -rf Exp3
	rm -rf *.jar.tmp

proper: clean
	rm -f $(ORIG_DATA) $(TARGET)