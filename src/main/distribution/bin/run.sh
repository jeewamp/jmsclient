#!/bin/sh
#------------------------------------------------------------------
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#----------------------------------------------------------------

#QUOTED_ARGS=""
#while [ "$1" != "" ] ; do

#  QUOTED_ARGS="$QUOTED_ARGS \"$1\""
#  shift

#done

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
           fi
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

if [ -z "$JMSC_HOME" ] ; then
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  saveddir=`pwd`

  JMSC_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  JMSC_HOME=`cd "$JMSC_HOME" && pwd`

  cd "$saveddir"
  # echo Using m2 at $JMSC_HOME
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JMSC_HOME" ] &&
    JMSC_HOME=`cygpath --unix "$JMSC_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JMSC_HOME" ] &&
    JMSC_HOME="`(cd "$JMSC_HOME"; pwd)`"
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
  # TODO classpath?
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$JMSC_HOME" ] &&
    JMSC_HOME=`cygpath --path --windows "$JMSC_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$HOME" ] &&
    HOME=`cygpath --path --windows "$HOME"`
fi

# ----- Process the input command ----------------------------------------------
for c in $*
do
    if [ "$c" = "--debug" ] || [ "$c" = "-debug" ] || [ "$c" = "debug" ]; then
          CMD="--debug"
          continue
    elif [ "$CMD" = "--debug" ]; then
          if [ -z "$PORT" ]; then
                PORT=$c
          fi
    fi
done

if [ "$CMD" = "--debug" ]; then
  if [ "$PORT" = "" ]; then
    echo " Please specify the debug port after the --debug option"
    exit 1
  fi
  if [ -n "$JAVA_OPTS" ]; then
    echo "Warning !!!. User specified JAVA_OPTS will be ignored, once you give the --debug option."
  fi
  JAVA_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=$PORT"
  echo "Please start the remote debugging client to continue..."
fi

JMSC_CLASSPATH=""
if [ -e "$JAVA_HOME/lib/tools.jar" ]; then
    JMSC_CLASSPATH="$JAVA_HOME/lib/tools.jar"
fi
for f in "$JMSC_HOME"/lib/*.jar
do
    if [ "$f" != "$JMSC_HOME/lib/*.jar" ];then
        JMSC_CLASSPATH="$JMSC_CLASSPATH":$f
    fi
done

JMSC_CLASSPATH="$JMSC_CLASSPATH":"${JMSC_HOME}"/conf/*
JMSC_CLASSPATH="$JMSC_CLASSPATH":"${JMSC_HOME}"/bin/*.jar

exec "$JAVACMD" \
  $JAVA_OPTS \
  -cp "$JMSC_CLASSPATH" \
  -Dconf.dir.path="$JMSC_HOME/conf" \
  org.wso2.testing.jmsclient.Main $*