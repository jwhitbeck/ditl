APPLET_TRACES="movement", "edges"
TRACE=pmtr

include ../include.mk

ORIG_DATA:=pmtr.txt
CRAWDAD_URL:=http://www.crawdad.org/download/unimi/pmtr/pmtr.txt.gz

trace: $(TRACE)/edges $(TRACE)/presence

movement: $(TRACE)/movement

all: trace movement

$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from CRAWDAD "
	@echo ""
	@read -p "CRAWDAD login: " login && \
	read -p "CRAWDAD password: " -s pass && \
	$(call GET,$(CRAWDAD_URL),$(ORIG_DATA).gz,$$login,$$pass)
	gunzip pmtr.txt.gz


$(TRACE)/edges: $(ORIG_DATA)
	@echo ""
	@echo "#################################################"
	@echo " Importing PMTR trace"
	@echo ""
	$(DITL) graphs import-edges \
		--orig-time-unit s \
		$(TRACE) pmtr.txt

$(TRACE)/presence: $(TRACE)/edges
	$(DITL) graphs edges-to-presence $(TRACE)

$(TRACE)/movement: $(TRACE)/edges $(TRACE)/presence
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $(TRACE) 1800
	$(DITL) plausible infer --update-interval 5 $(TRACE) 500 500
	$(DITL) rm $(TRACE) windowed_edges


proper:
	@rm -f $(ORIG_DATA)