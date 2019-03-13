package org.jetbrains.java.decompiler.main.decompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

public class SingleFileSaver implements IResultSaver {
   private final File target;
   private ZipOutputStream output;
   private Set<String> entries = new HashSet();

   public SingleFileSaver(File target) {
      this.target = target;
   }

   public void saveFolder(String path) {
      if (!"".equals(path)) {
         throw new UnsupportedOperationException("Targeted a single output, but tried to create a directory");
      }
   }

   public void copyFile(String source, String path, String entryName) {
      throw new UnsupportedOperationException("Targeted a single output, but tried to copy file");
   }

   public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
      throw new UnsupportedOperationException("Targeted a single output, but tried to save a class file");
   }

   public void createArchive(String path, String archiveName, Manifest manifest) {
      if (this.output != null) {
         throw new UnsupportedOperationException("Attempted to write multiple archives at the same time");
      } else {
         try {
            FileOutputStream stream = new FileOutputStream(this.target);
            this.output = (ZipOutputStream)(manifest != null ? new JarOutputStream(stream, manifest) : new ZipOutputStream(stream));
         } catch (IOException var5) {
            DecompilerContext.getLogger().writeMessage("Cannot create archive " + this.target, var5);
         }

      }
   }

   public void saveDirEntry(String path, String archiveName, String entryName) {
      this.saveClassEntry(path, archiveName, (String)null, entryName, (String)null);
   }

   public void copyEntry(String source, String path, String archiveName, String entryName) {
      if (this.checkEntry(entryName)) {
         try {
            ZipFile srcArchive = new ZipFile(new File(source));

            try {
               ZipEntry entry = srcArchive.getEntry(entryName);
               if (entry != null) {
                  InputStream in = srcArchive.getInputStream(entry);

                  try {
                     this.output.putNextEntry(new ZipEntry(entryName));
                     InterpreterUtil.copyStream(in, this.output);
                  } catch (Throwable var23) {
                     throw var23;
                  } finally {
                     if (in != null) {
                        in.close();
                     }

                  }
               }
            } catch (Throwable var25) {
               throw var25;
            } finally {
               srcArchive.close();
            }
         } catch (IOException var27) {
            String message = "Cannot copy entry " + entryName + " from " + source + " to " + this.target;
            DecompilerContext.getLogger().writeMessage(message, var27);
         }

      }
   }

   public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
      if (this.checkEntry(entryName)) {
         try {
            this.output.putNextEntry(new ZipEntry(entryName));
            if (content != null) {
               this.output.write(content.getBytes("UTF-8"));
            }
         } catch (IOException var8) {
            String message = "Cannot write entry " + entryName + " to " + this.target;
            DecompilerContext.getLogger().writeMessage(message, var8);
         }

      }
   }

   public void closeArchive(String path, String archiveName) {
      try {
         this.output.close();
         this.entries.clear();
         this.output = null;
      } catch (IOException var4) {
         DecompilerContext.getLogger().writeMessage("Cannot close " + this.target, IFernflowerLogger.Severity.WARN);
      }

   }

   private boolean checkEntry(String entryName) {
      boolean added = this.entries.add(entryName);
      if (!added) {
         String message = "Zip entry " + entryName + " already exists in " + this.target;
         DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
      }

      return added;
   }
}
