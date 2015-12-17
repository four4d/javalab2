package com.example.jinsungkim.myapplication;

/**
 * Created by jinsungkim on 15. 12. 17..
 */
public class Question {
    // 질문을 담을 변수
    String question;
    // 대한 답을 담을 변수
    int correctAnswer;
    // 사용자가 입력한 정답을 담을 변수. 처음엔 -1로 초기화
    int userAnswer;
    //이 변수들을 private으로 선언 후 get, set 함수를 만들어 프로그램하는 방법도 있지만
    //문제에서 주어진 형태를 그대로 유지하였습니다.


    // 질문, 정답으로 초기화하는 생성자
    public Question(String q, int answer) {
        question = q;
        correctAnswer = answer;
        userAnswer = -1;  // -1은 사용자 답 초기화하는 용도
    }

    // 정답과 사용자의 답을 비교
    public boolean isCorrect() {
        if(correctAnswer == userAnswer) {
            return true;
        } else {
            return false;
        }
    }

    // 질문을 리턴. 화면에 문제를 보여줄 때 사용한다.
    public String getQuestion() {
        return question;
    }

    // 사용자의 답을 셋팅한다.
    public void setUserAnswer(int answer) {
        userAnswer = answer;
    }

    // 사용자의 답을 -1로 초기화한다.
    public void resetAnswer() {
        userAnswer = -1;
    }
}
