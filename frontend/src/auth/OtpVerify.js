import React, { useState, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function OtpVerify() {
    const [otp, setOtp] = useState(["", "", "", ""]);
    const navigate = useNavigate();
    const inputs = useRef([]);

    const handleChange = (index, value) => {
        if (!/^\d?$/.test(value)) return;
        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);

        if (value && index < 3) {
            inputs.current[index + 1].focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === "Backspace" && !otp[index] && index > 0) {
            inputs.current[index - 1].focus();
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const enteredOtp = otp.join("");
        navigate('/reset/password');
        console.log("OTP entered:", enteredOtp);
        // navigate("/next-step");
    };

    return (
        <>
            <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: "450px", width: "100%", minHeight: "450px" }}>
                    <h3 className="mb-3">Verify OTP</h3>
                    <p className="text-muted mb-5">Enter 4 digit code sent to you at your email.</p>

                    <form onSubmit={handleSubmit}>
                        <div className="d-flex justify-content-between form-floating mb-3">
                            {otp.map((digit, i) => (
                                <input
                                    key={i}
                                    ref={el => inputs.current[i] = el}
                                    type="text"
                                    maxLength="1"
                                    value={digit}
                                    onChange={(e) => handleChange(i, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(i, e)}
                                    className="form-control text-center mx-1"
                                    style={{ width: "50px", fontSize: "24px", borderBottom: "", borderRadius: "0", background: "transparent" }}
                                    required
                                />
                            ))}
                        </div>

                        <div className="mt-13 mb-3">
                            <button type="submit" className="btn btn-dark w-100">Verify OTP</button>
                        </div>

                        <p className="text-center text-muted m-0">
                            Didn’t receive a verification code?
                        </p>
                        <div className="text-center m-0">
                            <Link className="btn btn-link p-0 text-dark">Resend Code</Link> |
                            <Link className="btn btn-link p-0 text-dark ms-1" to={"/forgot/password"}>Change Email</Link>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};