REACHABILITY_TRACES:=$(foreach d,1 2 3 4 5 6,edges_t1000_d$(d)000)

APPLET_TRACES="movement", "edges", "groups" $(foreach t,$(REACHABILITY_TRACES),", $(t)")
TRACE=rollernet

include ../include.mk

ORIG_DATA:=imote-traces-RollerNet.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/upmc/rollernet/imote-traces-RollerNet.tar.gz

trace: $(TRACE)/edges $(TRACE)/presence $(TRACE)/groups

movement: $(TRACE)/movement

reachability: $(TRACE)/edges_t1000_d10000

all: trace movement reachability

foo:
	echo $(DELAYS)

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
	@echo " Importing rollernet trace"
	@echo "   - Imported times are relative to first unix "
	@echo "     timestamp in the trace (1156083900)"
	@echo "   - Keep only iMote data (nodes 1-62)"
	@echo ""
	$(DITL) graphs import-edges --edges all_edges \
		--orig-time-unit s \
		--offset -1156083900 \
		$(TRACE) imote-traces-RollerNet/contacts.dat
	$(DITL) filter $(TRACE) all_edges edges [[1,62]]
	$(DITL) rm $(TRACE) all_edges

$(TRACE)/presence: $(TRACE)/edges
	$(DITL) graphs edges-to-presence $(TRACE)

$(TRACE)/groups: $(TRACE)/presence
	$(DITL) graphs import-groups $(TRACE) -- "[\
		{label:'members', members:[[1,26]]}, \
		{label:'staff', members:[[27,51]]}, \
		{label:'friends', members:[[52,62]]} ]"

$(TRACE)/movement: $(TRACE)/edges $(TRACE)/presence
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $(TRACE) 500
	$(DITL) plausible infer --constraints "{\
		LeftOutlier:{nodes:[38]},\
		Horizontal:{nodes:[38,27],height:25},\
		RightOutlier:{nodes:[27]}}"\
		 $(TRACE) 700 70
	$(DITL) rm $(TRACE) windowed_edges

$(TRACE)/edges_t1000_d10000: $(TRACE)/edges $(TRACE)/presence
	$(DITL) graphs reachability --min-delay 10 --every 10 $(TRACE) 1 1 60

clean:
	rm -rf imote-traces-RollerNet

proper:
	rm -f $(ORIG_DATA)