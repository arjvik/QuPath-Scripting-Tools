import qupath.lib.gui.scripting.QPEx
import qupath.lib.objects.PathObjects
import qupath.lib.objects.classes.PathClassFactory.StandardPathClasses
import qupath.lib.regions.ImagePlane
import qupath.lib.roi.ROIs

def plane = ImagePlane.getPlane(0, 0)
def ellipse = ROIs.createEllipseROI(0, 0, 100, 100, plane)
def ellipseAnno = PathObjects.createAnnotationObject(ellipse, StandardPathClasses.TUMOR.getPathClass())
def rectangle = ROIs.createRectangleROI(50, 50, 50, 50, plane)
def rectangleAnno = PathObjects.createAnnotationObject(rectangle, StandardPathClasses.STROMA.getPathClass())
QPEx.addObjects([ellipseAnno, rectangleAnno])
println "Success!"