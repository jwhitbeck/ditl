APPLET_TRACES="movement", "edges", "edges_t1000_d5000", "ccs"
TRACE=rwp

include ../include.mk

trace: $(TRACE)/movement $(TRACE)/presence $(TRACE)/edges $(TRACE)/ccs

reachability: $(TRACE)/edges_t1000_d5000

all: trace reachability

$(TRACE)/movement: $(TRACE)
	@[ -e $(TRACE) ] || mkdir $(TRACE)
	@$(DITL) graphs import-movement $(TRACE) $(DITL_DIR)/src/test/resources/$(TRACE)/movement.ns2

$(TRACE)/presence: $(TRACE)/movement
	@$(DITL) graphs movement-to-presence $(TRACE)

$(TRACE)/edges: $(TRACE)/movement
	@$(DITL) graphs movement-to-edges $(TRACE) 20

$(TRACE)/ccs: $(TRACE)/edges
	@$(DITL) graphs edges-to-ccs --groups ccs $(TRACE)

$(TRACE)/edges_t1000_d5000: $(TRACE)/edges
	@$(DITL) graphs reachability $(TRACE) 1 1 5