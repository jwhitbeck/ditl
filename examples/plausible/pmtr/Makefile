DITL_LIB:=../../../ditl.jar
PLAUSIBLE_LIB:=../../../plausible.jar

ORIG_DATA:=pmtr.txt
CRAWDAD_URL:=http://www.crawdad.org/download/unimi/pmtr/pmtr.txt.gz
TARGET:=pmtr.jar

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
	gunzip pmtr.txt.gz


$(TARGET): $(ORIG_DATA) InferPMTR.java
	@echo ""
	@echo "#################################################"
	@echo " Importing PMTR trace"
	@echo "   - Snapshots every hour"
	@echo ""
	$(DITL) graphs import-links \
		--orig-time-unit s \
		--snap-interval 3600 \
		$@ pmtr.txt
	$(DITL) graphs links-to-presence $@
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	javac $(JFLAGS) InferPMTR.java
	java $(JFLAGS) InferPMTR $@


clean:
	rm -rf *.class
	rm -rf *.jar.tmp

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)