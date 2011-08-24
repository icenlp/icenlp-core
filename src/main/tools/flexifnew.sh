
FLEXFILE=$1
JAVAFILEDIR=$2


JAVAFILE=`echo $(basename ${FLEXFILE}) | sed 's/\(.*\)\.flex/\1/'`
JAVAFILE=${JAVAFILEDIR}/${JAVAFILE}.java


# if java file does not exists or the flex file is new than java file
if [ ! -e $JAVAFILE  -o  $FLEXFILE -nt $JAVAFILE  ]
 then
 echo "Flexing file: ${FLEXFILE} JAVA FILE: ${JAVAFILE}"
 jflex -q ${FLEXFILE} -d ${JAVAFILEDIR}
 if [ "$?" -ne "0" ]; then
  echo "Error flexing file ${FLEXFILE}"
  echo "==============================================================="
  echo "Please make sure jflex is installed and in path."
  echo "If jflex runs out of heap space (very likely because of complex rules"
  echo "then increase the heap size that jflex uses. This can be done by"
  echo "modifying the jflex script (usually installed in /usr/bin/jflex) so that"
  echo "the line invoking JFlex.Main adds more heap space. For example by"
  echo "changing"
  echo "   \$JAVA JFlex.Main \$@"
  echo "to"
  echo "   \$JAVA -Xmx512m JFlex.Main \$@"
  echo "==============================================================="  
  exit 1
 fi
 else echo "Java file $(basename ${JAVAFILE}) is up to date"
fi
