
echo Flexing iceNER files
mkdir -p ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/iceNER 
for file in ${basedir}/src/main/jflex/iceNER/*.flex
  do ${basedir}/src/main/tools/flexifnew.sh $file ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/iceNER
    if [ "$?" -ne "0" ]; then
      echo "Error flexing iceNER files"
      exit 1
    fi
  done


echo Flexing iceparser files
mkdir -p ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/iceparser 
for file in ${basedir}/src/main/jflex/iceparser/*.flex
  do ${basedir}/src/main/tools/flexifnew.sh $file ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/iceparser
    if [ "$?" -ne "0" ]; then
      echo "Error flexing iceparser files"
      exit 1
    fi
  done


echo Flexing iceparser/errorSearch files
for file in ${basedir}/src/main/jflex/iceparser/errorSearch/??_errors.flex
  do ${basedir}/src/main/tools/flexifnew.sh $file ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/iceparser 
    if [ "$?" -ne "0" ]; then
      echo "Error flexing errorSearch files"
      exit 1
    fi
  done





echo Generating icetagger rules
mkdir -p ${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/icetagger
mkdir -p ${basedir}/target/generated-sources/intermedfiles

GENICERULESFLEX=${basedir}/src/main/jflex/icetagger/genIceRules.flex

GENICERULESJAVADIR=${basedir}/target/generated-sources/intermedfiles
GENICERULESJAVAFILE=`echo $(basename ${GENICERULESFLEX}) | sed 's/\(.*\)\.flex/\1/'`
GENICERULESJAVAFILE=${GENICERULESJAVADIR}/${GENICERULESJAVAFILE}.java

GENICERULESCLASSFILE=${GENICERULESJAVADIR}/genIceRules.class

ICELOCALRULESJAVADIR=${basedir}/target/generated-sources/flexfiles/is/iclt/icenlp/core/icetagger
ICELOCALRULESJAVAFILE=${ICELOCALRULESJAVADIR}/IceLocalRules.java
ICERULESFILE=${basedir}/src/main/jflex/icetagger/IceRules.rul


# If the genIceRules.java file does not exist or is older than genIceRules.flex
if [ ! -e ${GENICERULESJAVAFILE}  -o  ${GENICERULESFLEX} -nt ${GENICERULESJAVAFILE} ]
 then
   echo "Flexing file genIceRules.flex file"
   jflex -q ${GENICERULESFLEX} -d ${GENICERULESJAVADIR}
   if [ "$?" -ne "0" ]; then
     echo "Error flexing genIceRules.flex files"
     exit 1
   fi

   rm ${GENICERULESCLASSFILE}
   rm ${ICELOCALRULESJAVAFILE}
 else echo "Java file $(basename ${GENICERULESJAVAFILE}) is up to date"
fi


# If the genIceRules.class file does not exist or is older than genIceRules.java
if [ ! -e ${GENICERULESCLASSFILE}  -o  ${GENICERULESJAVAFILE} -nt ${GENICERULESCLASSFILE} ]
 then
   echo "Compiling file genIceRules.java"
   javac ${GENICERULESJAVAFILE}
   if [ "$?" -ne "0" ]; then
     echo "Error compiling file genIceRules.java"
     exit 1
   fi
   rm ${ICELOCALRULESJAVAFILE}
 else echo "Java class file $(basename ${GENICERULESCLASSFILE}) is up to date"
fi


# If the IceLocalRules.java file does not exist or is older than genIceRules.class
if [ ! -e ${ICELOCALRULESJAVAFILE}  -o  ${ICERULESFILE} -nt ${ICELOCALRULESJAVAFILE} ]
 then
 echo "Generating file IceLocalRules.java"
 java -cp ${GENICERULESJAVADIR} genIceRules ${ICERULESFILE} > ${ICELOCALRULESJAVAFILE}
 if [ "$?" -ne "0" ]; then
   echo "Error generating file IceLocalRules.java"
   exit 1
 fi
 else echo "Java class file $(basename ${ICELOCALRULESJAVAFILE}) is up to date"
fi

