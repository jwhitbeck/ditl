APPLET_TRACES="movement", "edges", "groups"
TRACE=stanford

include ../include.mk

ORIG_DATA:=moteFiles.zip roles.zip
MOTES_URL:=http://www.salathegroup.com/data/school_2010/moteFiles.zip
ROLES_URL:=http://www.salathegroup.com/data/school_2010/roles.zip

trace: $(TRACE)/edges $(TRACE)/presence

movement: $(TRACE)/movement

all: trace movement


$(ORIG_DATA):
	@echo "#################################################"
	@echo " Downloading data from www.salathegroup.com "
	@echo ""
	$(call GET,$(MOTES_URL),moteFiles.zip)
	$(call GET,$(ROLES_URL),roles.zip)

ImportStanford.class: ImportStanford.java
	@javac $(JFLAGS) $+

groups: roles.zip
	rm -f $@
	echo $$(grep student roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep staff roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep teacher roles/roles.txt | cut -f1) | tr ' ' , >> $@
	echo $$(grep other roles/roles.txt | cut -f1) | tr ' ' , >> $@

$(TRACE)/edges: $(ORIG_DATA) ImportStanford.class
	@echo ""
	@echo "#################################################"
	@echo " Decrompressing data"
	@echo ""
	unzip -u moteFiles.zip
	@echo ""
	@echo "#################################################"
	@echo " Importing Penn school trace"
	@echo "   - Keep the 9 hours between 7AM and 4PM"
	@echo ""
	java $(JFLAGS) ImportStanford $(TRACE) moteFiles/* 2>/dev/null
	$(DITL) graphs beacons-to-arcs --beacons rand_beacons $(TRACE)
	$(DITL) rm $(TRACE) rand_beacons
	$(DITL) graphs arcs-to-edges $(TRACE)
	$(DITL) rm $(TRACE) arcs
	$(DITL) graphs beacons-to-arcs --beacons beacons $(TRACE)
	$(DITL) rm $(TRACE) beacons
	$(DITL) graphs arcs-to-edges --edges tmp_edges $(TRACE)
	$(DITL) rm $(TRACE) arcs
	$(DITL) graphs buffer-edges --edges tmp_edges --buffered-edges sync_edges $(TRACE) 0 20
	$(DITL) rm $(TRACE) tmp_edges

$(TRACE)/presence: $(TRACE)/edges
	$(DITL) graphs edges-to-presence $(TRACE)

$(TRACE)/groups: $(TRACE)/presence $(ORIG_DATA)
	@unzip -u roles.zip
	@echo $$(grep student roles/roles.txt | cut -f1) | tr ' ' , > groups.student
	@echo $$(grep staff roles/roles.txt | cut -f1) | tr ' ' , > groups.staff
	@echo $$(grep teacher roles/roles.txt | cut -f1) | tr ' ' , > groups.teacher
	@echo $$(grep other roles/roles.txt | cut -f1) | tr ' ' , > groups.other
	$(DITL) graphs import-groups $(TRACE) -- "[\
		{label:'student', members:[$$(cat groups.student)] },\
		{label:'staff', members:[$$(cat groups.staff)] },\
		{label:'teacher', members:[$$(cat groups.teacher)] },\
		{label:'other', members:[$$(cat groups.other)] } ]"
	@rm -f groups.*

$(TRACE)/movement:
	@echo ""
	@echo "#################################################"
	@echo " Inferring movement (this can take several hours)"
	@echo ""
	$(DITL) plausible edges-to-windowed-edges $(TRACE) 1000
	$(DITL) plausible infer --n-steps 10 --tau 200 $(TRACE) 1000 1000
	$(DITL) rm $(TRACE) windowed_edges


clean:
	rm -f *.class
	rm -rf moteFiles roles __MACOSX

proper:
	rm -f $(ORIG_DATA)