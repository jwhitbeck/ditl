DITL_LIB:=../../../ditl.jar

ORIG_DATA:=moteFiles.zip roles.zip
MOTES_URL:=http://www.salathegroup.com/data/school_2010/moteFiles.zip
ROLES_URL:=http://www.salathegroup.com/data/school_2010/roles.zip
TARGET:=stanford.jar

DITL:=java -jar $(DITL_LIB)
JFLAGS:=-classpath "$(DITL_LIB):."

SHELL:=/bin/bash

.DEFAULT_GOAL:=$(TARGET)
.PHONY: clean proper


$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from www.salathegroup.com "
	@echo ""
	wget $(MOTES_URL)
	wget $(ROLES_URL)


groups: roles.zip
	rm -f $@
	S=$$(grep student roles/roles.txt | cut	 -f1) \
		&& echo -n "student:" >> $@ \
		&& echo $$S | tr ' ' : >> $@
	S=$$(grep staff roles/roles.txt | cut	 -f1) \
		&& echo -n "staff:" >> $@ \
		&& echo $$S | tr ' ' : >> $@
	S=$$(grep teacher roles/roles.txt | cut	 -f1) \
		&& echo -n "teacher:" >> $@ \
		&& echo $$S | tr ' ' : >> $@
	S=$$(grep other roles/roles.txt | cut	 -f1) \
		&& echo -n "other:" >> $@ \
		&& echo $$S | tr ' ' : >> $@



$(TARGET): $(ORIG_DATA) ImportStanford.java
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data"
	@echo ""
	unzip -u moteFiles.zip
	unzip -u roles.zip
	@echo ""
	@echo "#################################################"
	@echo " Importing Penn school trace"
	@echo "   - Snapshots every 15 minutes"
	@echo "   - Keep the 9 hours between 7AM and 4PM"
	@echo ""
	javac $(JFLAGS) ImportStanford.java
	java $(JFLAGS) ImportStanford $@ moteFiles/* 2>/dev/null
	$(DITL) graphs beacons-to-edges --snap-interval 900 $@ 20
	$(DITL) rm $@ beacons
	$(DITL) graphs edges-to-links $@
	$(DITL) rm $@ edges
	$(DITL) graphs links-to-presence $@
	make groups
	$(DITL) graphs import-groups --labels $@ $$(cat groups)
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