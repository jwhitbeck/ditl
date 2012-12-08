APPLET_TRACES="movement", "edges"
TRACE=infocom05

include ../include.mk

ORIG_DATA:=imote-trace3.tar.gz
CRAWDAD_URL:=http://www.crawdad.org/download/cambridge/haggle/imote-trace3.tar.gz

trace: $(TRACE)/edges $(TRACE)/presence

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
	@echo " Importing infocom'05 trace"
	@echo "   - Keep only iMote data (nodes 1-41)"
	@echo ""
	$(DITL) graphs import-arcs \
		--orig-time-unit s \
		$(TRACE) Exp3/contacts.Exp3.dat
	$(DITL) graphs arcs-to-edges --edges all_edges $(TRACE)
	$(DITL) rm $(TRACE) arcs
	$(DITL) filter $(TRACE) all_edges edges [[1,41]]
	$(DITL) rm $(TRACE) all_edges

$(TRACE)/presence: $(TRACE)/edges
	$(DITL) graphs edges-to-presence $(TRACE)


$(TRACE)/movement: $(TRACE)/presence
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $(TRACE) 1000
	$(DITL) plausible infer --update-interval 5 $(TRACE) 500 500
	$(DITL) rm $(TRACE) windowed_edges


clean:
	@rm -rf Exp3

proper:
	@rm -f $(ORIG_DATA)