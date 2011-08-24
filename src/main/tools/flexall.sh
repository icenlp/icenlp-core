
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




