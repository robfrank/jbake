package org.jbake.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jbake.launcher.Main;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Render output to a file.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Renderer {

	// TODO: should all content be made available to all templates via this class??
	
	private File source;
	private File destination;
	private Configuration cfg;
	
	private void render(Map<String, Object> model, String templateFilename, File outputFile) throws Exception {
		model.put("version", Main.VERSION);
		Template template = null;
		template = cfg.getTemplate(templateFilename);
		
		if (!outputFile.exists()) {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		}
		
		Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));
		template.process(model, out);
		out.close();
	}
	
	/**
	 * Creates a new instance of Renderer with supplied references to folders.
	 * 
	 * @param source		The source folder
	 * @param destination	The destination folder
	 * @param templatesPath	The templates folder
	 */
	public Renderer(File source, File destination, File templatesPath) {
		this.source = source;
		this.destination = destination;
		cfg = new Configuration();
		try {
			cfg.setDirectoryForTemplateLoading(templatesPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		
	}
	
	/**
	 * Render the supplied content to a file.
	 * 
	 * @param content	The content to render
	 */
	public void render(Map<String, Object> content) {
		String outputFilename = (new File((String)content.get("file")).getPath().replace(source.getPath()+File.separator+"content", destination.getPath()));
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));
		
		// delete existing versions if they exist in case status has changed either way
		File draftFile = new File(outputFilename+"-draft.html");
		if (draftFile.exists()) {
			draftFile.delete();
		}
		File publishedFile = new File(outputFilename+".html");
		if (publishedFile.exists()) {
			publishedFile.delete();
		}
		
		if (content.get("status").equals("draft")) {
			outputFilename = outputFilename + "-draft";
		}		
		File outputFile = new File(outputFilename+".html");
		
		System.out.print("Rendering [" + outputFile + "]... ");		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("content", content);
		
		try {
			render(model, ((String)content.get("type"))+".ftl", outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render an index file using the supplied content. 
	 * 
	 * @param posts		The content to render
	 * @param indexFile	The name of the output file
	 */
	public void renderIndex(List<Map<String, Object>> posts, String indexFile) {
		File outputFile = new File(destination.getPath() + File.separator + indexFile);
		System.out.print("Rendering index [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("posts", posts);
		
		try {
			render(model, "index.ftl", outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render an XML feed file using the supplied content.  
	 * 
	 * @param posts		The content to render
	 * @param feedFile	The name of the output file
	 */
	public void renderFeed(List<Map<String, Object>> posts, String feedFile) {
		File outputFile = new File(destination.getPath() + File.separator + feedFile);
		System.out.print("Rendering feed [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("posts", posts);
		model.put("pubdate", new Date());
		
		try {
			render(model, "feed.ftl", outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render an archive file using the supplied content.   
	 * 
	 * @param posts			The content to render
	 * @param archiveFile	The name of the output file 
	 */
	public void renderArchive(List<Map<String, Object>> posts, String archiveFile) {
		File outputFile = new File(destination.getPath() + File.separator + archiveFile);
		System.out.print("Rendering archive [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("posts", posts);
		
		try {
			render(model, "archive.ftl", outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render tag files using the supplied content. 
	 * 
	 * @param tags		The content to render
	 * @param tagPath	The output path
	 */
	public void renderTags(Map<String, List<Map<String, Object>>> tags, String tagPath) {
		for (String tag : tags.keySet()) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("tag", tag);
			// TODO: sort posts here
			List<Map<String, Object>> posts = Filter.getPublishedPosts(tags.get(tag));
			model.put("posts", posts);
			
			tag = tag.trim().replace(" ", "-");
			File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + ".html");
			System.out.print("Rendering tags [" + outputFile + "]... ");
			
			try {
				render(model, "tags.ftl", outputFile);
				System.out.println("done!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed!");
			}
		}
	}
}
