DITL_DIR:=../../
DITL_VERSION:=$(shell sed -n -r 's/.*<version>([^<]+)<\/version>.*/\1/p' $(DITL_DIR)/pom.xml | head -1)
DITL_LIB:=$(DITL_DIR)/target/ditl-$(DITL_VERSION).jar
DITL:=java -jar $(DITL_LIB)

SHELL:=/bin/bash

DITL:=java -jar $(DITL_LIB)
JFLAGS:=-cp $(DITL_LIB):.

WGET=wget --password $(4) --user $(3) -O $(2) $(1)
CURL=curl -u $(3):$(4) $(1) > $(2)
GET=$(WGET); if [ $$? -eq 127 ]; then $(CURL); fi

$(DITL_LIB):
	@cd $(DITL_DIR) && mvn package

.DEFAULT_GOAL:=trace
.PHONY: clean-player clean-jnlp all play trace reachability movement applet proper clean proper clean-applet init proper-trace

$(TRACE): | $(DITL_LIB)

Player.java:
	@[ ! -e $@ ] || rm $@;
	@echo 'import ditl.graphs.viz.GraphPlayer;' >> $@
	@echo 'import ditl.graphs.*;' >> $@
	@echo 'import ditl.Trace;' >> $@
	@echo 'public class Player {' >> $@
	@echo '    public static void main(String[] args){' >> $@
	@echo '	javax.swing.SwingUtilities.invokeLater(new Runnable() {' >> $@
	@echo '		public void run() {' >> $@
	@echo '		    GraphPlayer player = new GraphPlayer();' >> $@
	@echo '		    player.enableOpenButton(false);' >> $@
	@echo '		    player.loadTracesFromClassPath(new String[]{$(APPLET_TRACES)});' >> $@
	@echo '		}' >> $@
	@echo '	    });' >> $@
	@echo '    }' >> $@
	@echo '}' >> $@

Player.class: Player.java
	@javac $(JFLAGS) $+

clean-player:
	@rm -f Player*.{java,class}

APPLET_BASE_URL=http://plausible.lip6.fr
WIDTH=500
HEIGHT=500

$(TRACE).jnlp:
	@[ ! -e $@ ] || rm $@;
	@echo '<?xml version="1.0" encoding="UTF-8"?>' >> $@
	@echo ' <jnlp codebase="$(APPLET_BASE_URL) "href="$@">' >> $@
	@echo '     <information>' >> $@
	@echo '       <title>$(TRACE)</title>' >> $@
	@echo '       <vendor>Lip6</vendor>' >> $@
	@echo '       <offline-allowed />' >> $@
	@echo '     </information>' >> $@
	@echo '     <resources>' >> $@
	@echo '       <j2se version="1.6+" href="http://java.sun.com/products/autodl/j2se" />' >> $@
	@echo '       <jar href="ditl-$(DITL_VERSION).jar" main="false" />' >> $@
	@echo '       <jar href="$(TRACE).jar" main="false" />' >> $@
	@echo '       <jar href="$(TRACE)-player.jar" main="true" />' >> $@
	@echo '     </resources>' >> $@
	@echo '     <application-desc ' >> $@
	@echo '         name="$(TRACE)"' >> $@
	@echo '         main-class="Player"' >> $@
	@echo '         width="$(WIDTH)"' >> $@
	@echo '         height="$(HEIGHT)">' >> $@
	@echo '     </application-desc>' >> $@
	@echo ' </jnlp>' >> $@

clean-jnlp:
	@rm -f $(TRACE).jnlp

clean: clean-jnlp clean-player clean-applet

proper-trace:
	@rm -rf $(TRACE)

proper: clean proper-trace

play: $(TRACE)/movement
	@$(DITL) graphs play $(TRACE)

$(TRACE).jar: | all
	@jar cvMf $(TRACE).jar -C $(TRACE) .

manifest:
	@[ ! -e $@ ] || rm -f $@
	@echo "Main-Class: Player" >> $@
	@echo "Class-Path: ditl-$(DITL_VERSION).jar $(TRACE).jar ." >> $@

$(TRACE)-player.jar: manifest Player.class
	@jar cvmf manifest $@ *.class

applet: $(TRACE)-player.jar $(TRACE).jar $(TRACE).jnlp

clean-applet:
	@rm -f manifest $(TRACE)-player.jar $(TRACE).jar $(TRACE).jnlp