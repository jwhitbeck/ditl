APPLET_TRACES="movement", "edges", "groups"
TRACE=infocom06

include ../include.mk

ORIG_DATA:=Exp6.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/cambridge/haggle/Exp6.tar.gz

trace: $(TRACE)/edges $(TRACE)/presence $(TRACE)/groups

movement: $(TRACE)/movement

all: trace movement

$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from CRAWDAD "
	@echo ""
	@read -p "CRAWDAD login: " login && \
	read -p "CRAWDAD password: " -s pass && \
	$(call GET,$(CRAWDAD_URL),$(ORIG_DATA),$$login,$$pass)


$(TRACE)/edges: $(ORIG_DATA)
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data from CRAWDAD "
	@echo ""
	tar xzvf $<
	@echo ""
	@echo "#################################################"
	@echo " Importing infocom'06 trace"
	@echo "   - Keep only iMote data (nodes 1-98)"
	@echo ""
	$(DITL) graphs import-arcs \
		--orig-time-unit s \
		$(TRACE) Exp6/contacts.Exp6.dat
	$(DITL) graphs arcs-to-edges --edges all_edges $(TRACE)
	$(DITL) rm $(TRACE) arcs
	$(DITL) filter $(TRACE) all_edges edges [[1,98]]
	$(DITL) rm $(TRACE) all_edges

$(TRACE)/presence: $(TRACE)/edges
	$(DITL) graphs edges-to-presence $(TRACE)

$(TRACE)/groups: $(TRACE)/presence
	$(DITL) graphs import-groups \
			$(TRACE) -- "[\
			{label:'participants', members:[21,98]}, \
			{label:'mezzanine', members:[6,8,9,10,12,17]}, \
			{label:'floor -1', members:[3,14,16]}, \
			{label:'floor -2', members:[1,4,5,7,11,15]}, \
			{label:'lifts', members:[18,19,20]}, \
			{label:'bar', members:[2]}, \
			{label:'concierge',members:[13]} ]"

$(TRACE)/movement: $(TRACE)/presence $(TRACE)/edges
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) graphs import-positions --movement static_mv $(TRACE) -- "{\
			1:[150,350], 4:[200,350], 5:[150,300], 7:[100,400], \
			11:[150,400], 15:[100,350], 3:[250,150], 14:[300,200], \
			16:[250,250], 6:[400,300], 8:[300,450], 9:[400,400], \
			10:[370,350], 12:[450,350], 17:[300,350], 18:[250,325], \
			19:[250,350], 20:[250,375], 2:[350,280], 13:[300,300] }"
	$(DITL) plausible edges-to-windowed-edges $(TRACE) 1000
	$(DITL) plausible infer --update-interval 5 --known-movement static_mv --known-nodes [[1,20]] $(TRACE) 500 500
	$(DITL) rm $(TRACE) static_mv windowed_edges


clean:
	@rm -rf Exp6

proper:
	@rm -f $(ORIG_DATA)