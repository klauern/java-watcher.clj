(ns com.klauer.file.api
  (:use [potemkin :only [import-fn]])
  (:require [com.klauer.file.watcher :as watcher]))

(import-fn watcher/register)
(import-fn watcher/register-dir)
(import-fn watcher/register-dir-recursive)

