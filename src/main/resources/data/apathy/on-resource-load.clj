(ns apathy.load
  (:use apathy.api))

(log-msg "Hello, resource load!")

(set-rule! (deny-if (difficulty 'easy)))