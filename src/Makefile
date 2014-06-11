JAVAC=/usr/local/jdk1.5.0_06/bin/javac
OUTDIR=../build/classes
SRCDIR=.
CLASSPATH=../build/classes


all: clean
	mkdir -p "${OUTDIR}"
	${JAVAC} -d ${OUTDIR} -classpath ${CLASSPATH} -sourcepath ${SRCDIR} cockpitEditor/cockpitEditorMain.java

clean:
	test -n "${OUTDIR}" && rm -rf ${OUTDIR}/*

