package icu.etl.os.linux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import icu.etl.expression.GPatternExpression;
import icu.etl.os.OSFile;
import icu.etl.os.OSFileCommand;
import icu.etl.os.OSFileFilter;
import icu.etl.os.internal.OSFileImpl;
import icu.etl.util.Ensure;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.NetUtils;
import icu.etl.util.Settings;
import icu.etl.util.StringUtils;

public class LinuxFileCommand implements OSFileCommand {

    /** 当前所在目录文件 */
    protected File dir;

    public LinuxFileCommand() {
        super();
    }

    protected File get() {
        if (this.dir == null) {
            this.dir = Settings.getUserHome();
        }
        return this.dir;
    }

    public boolean rename(String filepath, String newfilepath) {
        return new File(FileUtils.replaceFolderSeparator(filepath)).renameTo(new File(FileUtils.replaceFolderSeparator(newfilepath)));
    }

    public boolean exists(String filepath) {
        return new File(FileUtils.replaceFolderSeparator(filepath)).exists();
    }

    public boolean isFile(String filepath) {
        return new File(FileUtils.replaceFolderSeparator(filepath)).isFile();
    }

    public boolean isDirectory(String filepath) {
        return new File(FileUtils.replaceFolderSeparator(filepath)).isDirectory();
    }

    public boolean mkdir(String filepath) {
        return FileUtils.createDirectory(FileUtils.replaceFolderSeparator(filepath));
    }

    public boolean rm(String filepath) {
        return FileUtils.delete(new File(FileUtils.replaceFolderSeparator(filepath)));
    }

    public boolean cd(String filepath) {
        File file = new File(filepath);
        if (file.exists() && file.isDirectory()) {
            this.dir = file;
            return true;
        } else {
            return false;
        }
    }

    public String pwd() {
        return this.get().getAbsolutePath();
    }

    public List<OSFile> ls(String filepath) {
        List<OSFile> files = new ArrayList<OSFile>();
        File[] list = StringUtils.isBlank(filepath) ? this.get().listFiles() : new File(FileUtils.replaceFolderSeparator(filepath)).listFiles();
        list = FileUtils.array(list);
        for (File file : list) {
            files.add(new OSFileImpl(file));
        }
        return files;
    }

    public String read(String filepath, String charsetName, int lineno) throws IOException {
        return FileUtils.readline(new File(FileUtils.replaceFolderSeparator(filepath)), charsetName, lineno);
    }

    public boolean write(String filepath, String charsetName, boolean append, CharSequence content) throws IOException {
        return FileUtils.write(new File(FileUtils.replaceFolderSeparator(filepath)), charsetName, append, content);
    }

    public boolean copy(String filepath, String directory) throws IOException {
        File file = new File(FileUtils.replaceFolderSeparator(filepath));
        return FileUtils.copy(file, new File(FileUtils.replaceFolderSeparator(directory), file.getName()));
    }

    public List<OSFile> find(String filepath, String name, char type, OSFileFilter filter) {
        List<OSFile> list = new ArrayList<OSFile>();
        File remotefile = new File(FileUtils.replaceFolderSeparator(filepath));
        if (remotefile.isDirectory()) {
            File[] files = FileUtils.array(remotefile.listFiles());
            if (files == null) {
                files = new File[0];
            }
            for (File file : files) {
                if (LinuxLocalOS.KEY_FILENAMES.contains(file.getName())) {
                    continue;
                }

                if (file.isDirectory()) {
                    if (type == 'd' && GPatternExpression.match(file.getName(), name)) {
                        OSFile osfile = new OSFileImpl(file);
                        if (filter == null || filter.accept(osfile)) {
                            list.add(osfile);
                        }
                    }

                    String dirctory = NetUtils.joinUri(file.getParent(), file.getName());
                    List<OSFile> clist = this.find(dirctory, name, type, filter);
                    list.addAll(clist);
                    continue;
                }

                if (file.isFile()) {
                    if (type == 'd') {
                        continue;
                    } else if (type == 'f') {
                        if (GPatternExpression.match(file.getName(), name)) {
                            OSFile osfile = new OSFileImpl(file);
                            if (filter == null || filter.accept(osfile)) {
                                list.add(osfile);
                            }
                            continue;
                        }
                    }
                }
            }
        } else {
            if (type == 'f' && GPatternExpression.match(remotefile.getName(), name)) {
                OSFile file = new OSFileImpl(remotefile);
                if (filter == null || filter.accept(file)) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public boolean upload(File localfile, String remotefilepath) throws IOException {
        File destfile = new File(FileUtils.replaceFolderSeparator(remotefilepath));
        if (destfile.exists()) {
            if (destfile.isDirectory()) {
                return FileUtils.copy(localfile, new File(destfile, localfile.getName()));
            } else {
                return FileUtils.copy(localfile, destfile);
            }
        } else {
            File parentfile = destfile.getParentFile();
            if (parentfile.exists()) {
                if (parentfile.isDirectory()) {
                    return FileUtils.copy(localfile, new File(parentfile, localfile.getName()));
                } else {
                    return false;
                }
            } else {
                if (FileUtils.createDirectory(parentfile)) {
                    return FileUtils.copy(localfile, new File(parentfile, localfile.getName()));
                } else {
                    return false;
                }
            }
        }
    }

    public File download(String remotefilepath, File localfile) throws IOException {
        File file = new File(FileUtils.replaceFolderSeparator(remotefilepath));
        if (!file.exists()) {
            return null;
        }

        if (localfile.exists()) {
            if (localfile.isDirectory()) {
                File copyfile = new File(localfile, file.getName());
                return FileUtils.copy(file, copyfile) ? copyfile : null;
            } else {
                if (file.isFile()) {
                    return FileUtils.copy(file, localfile) ? localfile : null;
                } else {
                    return null;
                }
            }
        } else {
            FileUtils.assertCreateDirectory(localfile);
            File copyfile = new File(localfile, file.getName());
            return FileUtils.copy(file, copyfile) ? copyfile : null;
        }
    }

    public String getCharsetName() {
        return Settings.getFilenameEncoding();
    }

    public void setCharsetName(String charsetName) {
        System.setProperty("sun.jnu.encoding", charsetName);
    }

    public boolean upload(InputStream in, String remote) throws IOException {
        FileOutputStream out = new FileOutputStream(new File(remote));
        IO.write(in, out);
        return true;
    }

    public boolean download(String remote, OutputStream out) throws IOException {
        FileInputStream in = new FileInputStream(new File(remote));
        IO.write(in, out);
        return true;
    }

}
