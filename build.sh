#!/bin/sh
FLITE_APP_DIR="$(dirname $(readlink -f "$0"))"
FLITE_BUILD_DIR="${FLITE_APP_DIR}/flite"
FLITE_ARCHIVE_URL="http://www.festvox.org/flite/packed/flite-2.0/flite-2.0.0-release.tar.bz2"
FLITE_ARCHIVE_MD5="645db96ffc296cbb6d37f231cc1cc6b2"
FLITE_ARCHIVE_NAM="$(basename "${FLITE_ARCHIVE_URL}")"
FLITE_ARCHIVE_DIR="$(basename "${FLITE_ARCHIVE_URL}" .tar.bz2)"

# Abort after first error
set -e
OLDPWD="${PWD}"

# Check for required environment vairables
HAVE_REQUIRED_ENVS=true

if [ -z "${ANDROID_NDK}" ] && [ -z "${ANDROID_NDK_HOME}" ];
then
	echo "Missing Android NDK path environment variable: ANDROID_NDK / ANDROID_NDK_HOME" >&2
	HAVE_REQUIRED_ENVS=false
fi

if [ -z "${ANDROID_SDK}" ] && [ -z "${ANDROID_HOME}" ];
then
	echo "Missing Android SDK path environment variable: ANDROID_SDK / ANDROID_HOME" >&2
	HAVE_REQUIRED_ENVS=false
fi

if ! ${HAVE_REQUIRED_ENVS};
then
	exit 1
fi

export FLIGHT_APP_DIR

# Download and patch flight, unless it was provided
if [ -z "${FLITEDIR}" ];
then
	# Check if the actual `flight` build directory exists
	flite_directory="${FLITE_BUILD_DIR}/${FLITE_ARCHIVE_DIR}"
	if ! [ -d "${flite_directory}" ];
	then
		mkdir -p "${FLITE_BUILD_DIR}"
		
		flite_archive="${FLITE_BUILD_DIR}/${FLITE_ARCHIVE_NAM}"
		
		# Download the `flight` file archive
		if ! [ -e "${flite_archive}" ];
		then
			wget "${FLITE_ARCHIVE_URL}" -O "${flite_archive}"
		fi
		
		# Verify the archive's integrity
		echo "${FLITE_ARCHIVE_MD5}  ${flite_archive}" | md5sum --check -
		
		# Extract the `flight` file archive
		# tar -C "${FLITE_BUILD_DIR}" -xvf "${flite_archive}"
		
		# Patch `flight` configure script to work with newer versions of the NDK (10e – 11c tested)
		# I pulled out the patch and just edited the file directly
		cd "${flite_directory}"
		autoreconf
		cd "${OLDPWD}"
		
	fi
	
	export FLITEDIR="${flite_directory}"
fi

# Build `flight` engine for all supported targets
cd "${FLITEDIR}"
for arch in aarch64 armeabiv7a x86 x86_64;
do
	if ! [ -e "${FLITEDIR}/build/${arch}-none/lib/libflite.a" ];
	then
		./configure --with-langvox=android --target="${arch}"
		make -j4
	fi
done
cd "${OLDPWD}"

# This part should be pretty irrelivent, since it's making the APK not the compiled files. Those are above
# Build the Android application package. up to this point, flite should be built yeah? It's just a matter of making the APK
if [ $# -gt 0 ];
then
	action="${1}"
	shift 1
else
	action="debug"
fi
ant "${action}" "$@"
