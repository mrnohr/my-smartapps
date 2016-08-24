/**
 * Quick and dirty script to combine all the README files from the child directories
 */
import groovy.io.FileType

File currentDir = new File(".")
def dirs = []
currentDir.eachFile FileType.DIRECTORIES, {
    dirs << it.name
}

new File("README.md", currentDir).text = dirs.collect {
	def readme = new File("$it/README.md", currentDir)
	if(readme.exists()) {
		return """
# ${it - '.src'}

${readme.text}
		"""
	}
}.join()
