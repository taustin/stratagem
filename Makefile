# rec_wildcard
#
# A recursive "wildcard" function for GNU Make that searches a directory and
# all its subdirectories for files matching a pattern.
#
# To find all the C files in the current directory:
#   $(call rwildcard, , *.c)
# To find all the .c and .h files in src:
#   $(call rwildcard, src/, *.c *.h)
#
# https://stackoverflow.com/questions/2483182/recursive-wildcards-in-gnu-make
rec_wildcard=$(foreach d,$(wildcard $1*),$(call rec_wildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# Update these variables to match the locations
JUNIT_JAR=lib/junit-4.12.jar
HAMCREST_JAR=lib/hamcrest-core-1.3.jar
ANTLR_JAR=lib/antlr-4.6-complete.jar

TEST_CLASSPATH=${JUNIT_JAR}:${HAMCREST_JAR}:${ANTLR_JAR}

GRAMMAR_NAME=Stratagem
GRAMMAR=${GRAMMAR_NAME}.g4

SRC_BASE_DIR=src
TEST_SRC_BASE_DIR=testSrc
GEN_SRC_BASE_DIR=generatedSrc
# Choosing build instead of bin to avoid conflicts with Eclipse
BUILD_DIR=build
STRATAGEM_SCRIPT_DIR=stratagemScripts
TREES_DIR=parseTrees

GEN_SRC_DIR=${GEN_SRC_BASE_DIR}/${PARSER_SRC_FOLDERS}

SRC_FOLDERS=edu/sjsu/stratagem
PACKAGE_NAME=edu.sjsu.stratagem
PARSER_SRC_FOLDERS=edu/sjsu/stratagem/parser
PARSER_PACKAGE_NAME=edu.sjsu.stratagem.parser

SCRIPTS=$(call rec_wildcard, ${STRATAGEM_SCRIPT_DIR}, *.strata)
MAIN_SOURCES=$(call rec_wildcard, ${SRC_BASE_DIR}, *.java)
TEST_SOURCES=$(call rec_wildcard, ${TEST_SRC_BASE_DIR}, *.java)
GENERATED_SOURCES=$(call rec_wildcard, ${GEN_SRC_BASE_DIR}, *.java)
SOURCES=${MAIN_SOURCES} ${TEST_SOURCES} ${GENERATED_SOURCES}


.PHONY: all generate compile parse test run clean spotless

# By default, compile the sources and stop. For something more interesting look
# at parse, test, and run.
all: generate compile

# Run ANTLR on the grammar file to generate sources for a Java lexer/parser
generate: ${GEN_SRC_BASE_DIR}/.build-timestamp

# Compile Java sources
compile: ${BUILD_DIR}/.build-timestamp

# Visually present parse trees for all test Stratagem scripts
parse: generate compile ${TREES_DIR}
	$(foreach script, ${SCRIPTS}, \
		java -cp ${BUILD_DIR}:${ANTLR_JAR} \
		     org.antlr.v4.gui.TestRig \
		     ${PARSER_PACKAGE_NAME}.${GRAMMAR_NAME} \
		     prog \
		     -gui \
		     ${script} > ${TREES_DIR}/$(notdir ${script}).tree; \
	)

# Run Java unit tests
test: generate compile
	java -cp ${BUILD_DIR}:${TEST_CLASSPATH} \
	     org.junit.runner.JUnitCore \
	     ${PACKAGE_NAME}.CastTest \
	     ${PACKAGE_NAME}.ExpressionTest \
	     ${PACKAGE_NAME}.ValueTest

# Run the interpretor on all test Stratagem scripts
run: generate compile
	$(foreach script, ${SCRIPTS}, \
		echo; \
		echo "Running ${script}"; \
		java -cp ${BUILD_DIR}:${ANTLR_JAR} \
		     ${PACKAGE_NAME}.Interpreter ${script}; \
	)

clean:
	rm -rf ${GEN_SRC_BASE_DIR} ${BUILD_DIR}

spotless: clean
	rm -rf ${TREES_DIR}


# Same timestamp technique as below
${GEN_SRC_BASE_DIR}/.build-timestamp: ${GEN_SRC_DIR} ${GRAMMAR}
	java -jar ${ANTLR_JAR} \
	     -no-listener \
	     -visitor \
	     ${GRAMMAR} \
	     -o ${GEN_SRC_DIR}
	@touch ${GEN_SRC_BASE_DIR}/.build-timestamp

${GEN_SRC_DIR}:
	mkdir -p ${GEN_SRC_DIR}

# Use an arbitrary file (".build-timestamp") to keep track of when we last
# compiled. We update its "last modified" timestamp on the filesystem after we
# compile. Make will avoid running this target's commands if our file has a
# newer timestamp than every source file.
${BUILD_DIR}/.build-timestamp: ${BUILD_DIR}/${SRC_FOLDERS} ${SOURCES}
	javac -cp ${TEST_CLASSPATH} -d ${BUILD_DIR} ${SOURCES} -Xlint:unchecked -g
	@touch build/.build-timestamp

${BUILD_DIR}/${SRC_FOLDERS}:
	mkdir -p ${BUILD_DIR}/${SRC_FOLDERS}

${TREES_DIR}:
	mkdir -p ${TREES_DIR}

# Force Make to restart if the ${GEN_SRC_BASE_DIR}/.build-timestamp file is
# modified.
#
# This causes Make to re-evaluate ${SOURCES} and find the newly generated ANTLR
# sources if we are running the `generate` and `compile` steps one after the
# other.
#
# https://stackoverflow.com/questions/554742/reevaluate-makefile-variables
-include ${GEN_SRC_BASE_DIR}/.build-timestamp
