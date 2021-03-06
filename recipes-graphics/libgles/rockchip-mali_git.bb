DESCRIPTION = "GLES libraries for Rockchip SoCs for a family of Mali GPU"
SECTION = "libs"
LICENSE = "BINARY"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=564e729dd65db6f65f911ce0cd340cf9"
NO_GENERIC_LICENSE[BINARY] = "LICENSE.TXT"

COMPATIBLE_MACHINE = "(rk3036|rk3288|rk3328|rk3399)"

# There's only hardfp version available
python __anonymous() {
    if d.getVar("TUNE_FEATURES", True) == "aarch64":
        return
    tunes = d.getVar("TUNE_FEATURES", True)
    if not tunes:
        return
    if "callconvention-hard" not in tunes:
        pkgn = d.getVar("PN", True)
        pkgv = d.getVar("PV", True)
        raise bb.parse.SkipPackage("%s-%s ONLY supports hardfp mode for now" % (pkgn, pkgv))
}

DEPENDS = "libdrm mesa"

PROVIDES += "virtual/egl virtual/libgles1 virtual/libgles2 virtual/libopencl libgbm"
PROVIDES += "${@bb.utils.contains("DISTRO_FEATURES", "wayland", " virtual/libwayland-egl", " ", d)}"

S = "${WORKDIR}/git"

SRC_URI = "git://github.com/rockchip-linux/libmali.git;branch=rockchip;"
SRCREV_pn-${PN} = "796ef53caf01afea545115fad7393d42c78d96aa"

INSANE_SKIP_${PN} = "already-stripped ldflags dev-so"

INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_PACKAGE_STRIP = "1"

USE_X11 = "${@bb.utils.contains("DISTRO_FEATURES", "x11", "yes", "no", d)}"
USE_WL = "${@bb.utils.contains("DISTRO_FEATURES", "wayland", "yes", "no", d)}"

MALI_X11_rk3036 = "arm-linux-gnueabihf/libmali-utgard-400-r6p0.so"
MALI_WAYLAND_rk3036 = "arm-linux-gnueabihf/libmali-utgard-400-r7p0-wayland.so"
MALI_GBM_rk3036 = "arm-linux-gnueabihf/libmali-utgard-400-r7p0-gbm.so"

MALI_X11_rk3288 = "arm-linux-gnueabihf/libmali-midgard-t76x-r9p0-r0p0.so"
MALI_WAYLAND_rk3288 = "arm-linux-gnueabihf/libmali-midgard-t76x-r13p0-r0p0-wayland.so "
MALI_GBM_rk3288 = "arm-linux-gnueabihf/libmali-midgard-t76x-r13p0-r0p0-gbm.so "

MALI_X11_rk3328 = "aarch64-linux-gnu/libmali-utgard-450-r7p0.so"
MALI_WAYLAND_rk3328 = "aarch64-linux-gnu/libmali-utgard-450-r7p0-wayland.so"
MALI_GBM_rk3328 = "aarch64-linux-gnu/libmali-utgard-450-r7p0-gbm.so"

MALI_X11_rk3399 = "aarch64-linux-gnu/libmali-midgard-t86x-r9p0.so"
MALI_WAYLAND_rk3399 = "aarch64-linux-gnu/libmali-midgard-t86x-r13p0-wayland.so"
MALI_GBM_rk3399 = "aarch64-linux-gnu/libmali-midgard-t86x-r9p0-gbm.so"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
	# Create MALI manifest
	install -m 755 -d ${D}/${libdir}
	if [ "${USE_WL}" = "yes" ]; then
		install ${S}/lib/${MALI_WAYLAND} ${D}/${libdir}/libMali.so
	elif [ "${USE_X11}" = "yes" ]; then
		install ${S}/lib/${MALI_X11} ${D}/${libdir}/libMali.so
	else
		install ${S}/lib/${MALI_GBM} ${D}/${libdir}/libMali.so
	fi

	ln -sf libMali.so ${D}/${libdir}/libEGL.so
	ln -sf libMali.so ${D}/${libdir}/libEGL.so.1
	ln -sf libMali.so ${D}/${libdir}/libGLESv1_CM.so
	ln -sf libMali.so ${D}/${libdir}/libGLESv1_CM.so.1
	ln -sf libMali.so ${D}/${libdir}/libGLESv2.so
	ln -sf libMali.so ${D}/${libdir}/libGLESv2.so.2
	ln -sf libMali.so ${D}/${libdir}/libOpenCL.so
	ln -sf libMali.so ${D}/${libdir}/libOpenCL.so.1
	ln -sf libMali.so ${D}/${libdir}/libgbm.so
	ln -sf libMali.so ${D}/${libdir}/libgbm.so.1

	if [ "${USE_WL}" = "yes" ]; then
		ln -sf libMali.so ${D}/${libdir}/libwayland-egl.so
	fi
}

PACKAGES = "${PN}"
FILES_${PN} += "${libdir}/*.so"

RREPLACES_${PN} = "libegl libgles1 libglesv1-cm1 libgles2 libglesv2-2 libgbm"
RCONFLICTS_${PN} = "libegl libgles1 libglesv1-cm1 libgles2 libglesv2-2 libgbm"
RPROVIDES_${PN} += "libegl libgles1 libglesv1-cm1 libgles2 libglesv2-2 libgbm"

# Workaround: libMali.so provided by rk having no SONAME field in it
# so add it to fix rdepends problems
RPROVIDES_${PN} += "libwayland-egl.so libgbm.so libGLESv1_CM.so libGLESv2.so libEGL.so libOpenCL.so"
RPROVIDES_${PN}_aarch64 += "libwayland-egl.so()(64bit) libgbm.so()(64bit) libGLESv1_CM.so()(64bit) \
	libGLESv2.so()(64bit) libEGL.so()(64bit) libOpenCL.so()(64bit) "