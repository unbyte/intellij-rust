/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.debugger

import com.jetbrains.cidr.execution.debugger.CidrDebugProcess
import com.jetbrains.cidr.execution.debugger.formatters.natvis.NatvisFileProvider
import org.rust.cargo.project.settings.toolchain
import org.rust.debugger.LLDBRenderers.COMPILER
import org.rust.debugger.runconfig.RsDebugProcessConfigurator
import org.rust.debugger.settings.RsDebuggerSettings
import java.io.File

class RustcNatvisFileProvider : NatvisFileProvider {
    override fun populate(debugProcess: CidrDebugProcess, fileList: MutableList<String>) {
        val settings = RsDebuggerSettings.getInstance()
        if (settings.lldbRenderers == COMPILER) {
            val toolchain = debugProcess.project.toolchain ?: return
            val cargoProject = RsDebugProcessConfigurator.findCargoProject(debugProcess) ?: return
            val sysroot = cargoProject.rustcInfo?.sysroot
                ?.let { toolchain.toRemotePath(it) }
                ?: return
            val visualizersDir = File(sysroot).resolve("lib").resolve("rustlib").resolve("etc")
            val absolutePaths = NatvisFileProvider.listNatvisFilesInDirectory(visualizersDir).map { it.absolutePath }
            fileList.addAll(absolutePaths)
        }
    }
}
