DITL_LIB:=../../../ditl.jar

ORIG_DATA:=pmtr.txt
CRAWDAD_URL:=http://www.crawdad.org/download/unimi/pmtr/pmtr.txt.gz
TARGET:=pmtr.jar

DITL:=java -jar $(DITL_LIB)

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


$(TARGET): $(ORIG_DATA)
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
	$(DITL) plausible links-to-windowed-links $@ 1800
	$(DITL) plausible infer --update-interval 5 $@ 500 500
	$(DITL) rm $@ windowed_links


clean:
	rm -rf *.jar.tmp

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)