import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import AuthBackground from "../components/AuthBackground";

export default function ResetPassword() {

    const [formData, setFormData] = useState({ newPassword: "", confirmPassword: "" });
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();
    const { email } = location.state || {};

    const resetForm = () => {
        setFormData({ newPassword: "", confirmPassword: "" });
    }

    const toggle = () => {
        setShowPassword(!showPassword);
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.newPassword !== formData.confirmPassword) {
            alert("Passwords do not match");
            return;
        }

        try {
            const { data, status } = await axios.put("http://localhost:8081/user/reset/password",
                { email, newPassword: formData.newPassword }
            );
            if (status === 200) {
                alert(data.message);
                navigate("/login");
            }
        } catch (err) {
            if (err.data?.error) {
                alert(`${err.data.error}`);
                console.log(err.data.error);
            } else {
                alert('Reset password failed due to server error.');
            }
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <AuthBackground>

                <h3 className="mb-3">Reset Password</h3>

                <p className="text-muted mb-3">Please enter your password and confirm the password.</p>

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="text-6c757d">New Password</label>
                        <input type="password" name="newPassword" className="form-control mb-1" placeholder="" value={formData.newPassword} onChange={handleChange} required />
                    </div>

                    <div className="mb-3 position-relative">
                        <div className="">
                            <label className="text-6c757d">Confirm Password</label>
                            <input type={showPassword ? "text" : "password"} name="confirmPassword" className="form-control" placeholder="" value={formData.confirmPassword} onChange={handleChange} required />
                        </div>
                        <span
                            onClick={toggle}
                            style={{ position: "absolute", right: "15px", top: "65%", transform: "translateY(-50%)", cursor: "pointer", color: "#6c757d" }}>
                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                        </span>
                    </div>

                    <div className="mb-3">
                        <button type="submit" className="btn btn-dark w-100">Save</button>
                    </div>
                </form>

            </AuthBackground>
        </>
    );
};