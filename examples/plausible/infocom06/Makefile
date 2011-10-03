DITL_LIB:=../../../ditl.jar

ORIG_DATA:=Exp6.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/cambridge/haggle/Exp6.tar.gz
TARGET:=infocom06.jar

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


$(TARGET): $(ORIG_DATA)
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data from CRAWDAD "
	@echo ""
	tar xzvf $<
	@echo ""
	@echo "#################################################"
	@echo " Importing infocom'06 trace"
	@echo "   - Snapshots every 30 minutes"
	@echo "   - Keep only iMote data (nodes 1-98)"
	@echo ""
	$(DITL) graphs import-edges \
		--orig-time-unit s \
		--snap-interval 1800 \
		$@ Exp6/contacts.Exp6.dat
	$(DITL) graphs edges-to-links --links all_links $@
	$(DITL) rm $@ edges
	$(DITL) filter $@ all_links links 1-98
	$(DITL) rm $@ all_links
	$(DITL) graphs links-to-presence $@
	$(DITL) graphs import-groups --labels $@ \
			"participants:21-98" \
			"mezzanine:6:8:9:10:12:17" \
			"floor -1:3:14:16" \
			"floor -2:1:4:5:7:11:15" \
			"lifts:18:19:20" \
			"bar:2" \
			"concierge:13"
	$(DITL) graphs import-positions --movement static_mv $@ \
			1:150:350 4:200:350 5:150:300 7:100:400 11:150:400 15:100:350 \
			3:250:150 14:300:200 16:250:250 \
			6:400:300 8:300:450 9:400:400 10:370:350 12:450:350 17:300:350 \
			18:250:325 19:250:350 20:250:375 \
			2:350:280 13:300:300
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible links-to-windowed-links $@ 1000
	$(DITL) plausible infer --update-interval 5 --known-movement static_mv --known-nodes 1-20 $@ 500 500
	$(DITL) rm $@ static_mv windowed_links


clean:
	rm -rf Exp6
	rm -rf *.jar.tmp

proper: clean
	rm -f $(TARGET) $(ORIG_DATA)