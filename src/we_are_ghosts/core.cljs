(ns ^:figwheel-always we-are-ghosts.core
    (:require))

(enable-console-print!)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

; ugh
(def controls (atom nil))

(def headset (atom false))

(defn rnd [] (.random js/Math))

(def pi js/Math.PI)

(defn init []
  (print "init")
  
  (let [clock (js/THREE.Clock.)
        renderer (js/THREE.WebGLRenderer.)
        element renderer.domElement
        container (.getElementById js/window.document "viewport")
        effect (js/THREE.StereoEffect. renderer)
        scene (js/THREE.Scene.)
        camera (js/THREE.PerspectiveCamera. 90 1 0.001 700)
        light (js/THREE.HemisphereLight. 0x777777 0x000000 1.0)
        ambient-light (js/THREE.AmbientLight. 0x0c0c0c)
        material (js/THREE.MeshPhongMaterial. {:color 0x88ff88
                                              :specular 0x888888
                                              :shininess 10
                                              :shading js/THREE.FlatShading
                                              ; :map texture
                                              })
        geometry-floor (js/THREE.PlaneGeometry. 1000 1000)
        geometry-cube (js/THREE.BoxGeometry. 20 20 20)
        mesh (js/THREE.Mesh. geometry-floor material)]
    
    ; set up our initial controls which we might swap out later
    ; if it turns out we are on a mobile device (undetectable at load time)
    (reset! controls (js/THREE.OrbitControls. camera element))
    
    (set! (.-innerHTML container) "")
    (.appendChild container element);

    (.set camera.position 0 10 0);
    (.add scene camera)

    (.rotateUp @controls (/ js/Math.PI 4))
    (.set (.-target @controls)
        (+ camera.position.x 0.1)
        camera.position.y
        camera.position.z)
    (set! (.-noZoom @controls) true)
    (set! (.-noPan @controls) true)
    
    (.add scene light)
    (.add scene ambient-light)
    
    (set! (.-x mesh.rotation) (* (/ js/Math.PI 2) -1))
    (.add scene mesh)

    ; add cubes
    (doseq [i (range 50)]
      (let [o (js/THREE.Mesh. geometry-cube (js/THREE.MeshLambertMaterial. {:color (* (rnd) 0xffffff)}))]
        (set! (.-x (.-position o)) (- (* 400 (rnd)) 200))
        (set! (.-y (.-position o)) (- (* 10 (rnd)) 5))
        (set! (.-z (.-position o)) (- (* 400 (rnd)) 200))

        (set! (.-x (.-rotation o)) (* 2 pi (rnd)))
        (set! (.-y (.-rotation o)) (* 2 pi (rnd)))
        (set! (.-z (.-rotation o)) (* 2 pi (rnd)))
        
        (set! (.-x (.-scale o)) (* 0.5 (rnd)))
        (set! (.-y (.-scale o)) (* 0.5 (rnd)))
        (set! (.-z (.-scale o)) (* 0.5 (rnd)))
        (.add scene o)))

    ; add trees
    (doseq [i (range 30)]
      (let [o (js/THREE.Mesh. (js/THREE.OctahedronGeometry. 10) (js/THREE.MeshLambertMaterial. {:color 0x00ff00}))]
        (set! (.-x (.-position o)) (- (* 400 (rnd)) 200))
        ; (set! (.-y (.-position o)) (- (* 10 (rnd)) 5))
        (set! (.-z (.-position o)) (- (* 400 (rnd)) 200))
        
        (let [base-size (+ 0.5 (rnd))]
          (set! (.-x (.-scale o)) base-size)
          (set! (.-z (.-scale o)) base-size))
        (set! (.-y (.-scale o)) (+ 5.0 (rnd)))
        
        (.setHex (.-emissive (.-material o)) 0x001100)
        (.add scene o)))
    
    ; add a moon
    (let [moon (js/THREE.Mesh. (js/THREE.SphereGeometry. 50.0 32 32) (js/THREE.MeshLambertMaterial. {:color 0xffffff}))]
      (set! (.-y (.-position moon)) 100)
      (set! (.-x (.-position moon)) 100)
      (.setHex (.-emissive (.-material moon)) 0x111111)
      (.add scene moon))

    ; *** Functions *** ;
    
    (defn resize []
      (let [width container.offsetWidth
            height container.offsetHeight]
        (set! (.-aspect camera) (/ width height))
        (.updateProjectionMatrix camera)
        (.setSize renderer width height)
        (.setSize effect width height)))

    (defn update-frame [dt]
      (resize)
      (.updateProjectionMatrix camera)
      (.update @controls dt))

    (defn render-frame [dt]
      (if @headset
        (.render effect scene camera)
        (.render renderer scene camera)))

    (defn animate [t]
      (js/requestAnimationFrame animate)
      (update-frame (.getDelta clock))
      (render-frame (.getDelta clock)))

    (defn fullscreen [ev]
      (cond (aget container "requestFullscreen") (.requestFullscreen container)
            (aget container "webkitRequestFullscreen") (.webkitRequestFullscreen container)))
    
    (defn setOrientationControls [e]
      (when (aget e "alpha")
        (reset! headset true)
        (let [new-controls (js/THREE.DeviceOrientationControls. camera true)]
          (.connect new-controls)
          (.update new-controls)
          (.removeEventListener js/window "deviceorientation" setOrientationControls true)
          (reset! controls new-controls))
        (.addEventListener element "click" fullscreen false)))
    
    ; *** Launch *** ;
    
    (.addEventListener js/window "deviceorientation" setOrientationControls true)

    (.addEventListener js/window "resize" resize false)
    (js/setTimeout resize 1)

    (animate 0)))

(init)
